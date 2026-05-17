package com.iliyadev.springboot.models

import java.time.Instant
import java.util.UUID
import com.iliyadev.springboot.models.Poll
import com.fasterxml.jackson.annotation.JsonFormat

// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 
// ðŸ”  Auth DTOs
// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 

data class SendOtpRequest(
    val phoneNumber: String
)

data class SendOtpResponse(
    val success: Boolean,
    val message: String,
    val expiresInSeconds: Int = 60
)

data class VerifyOtpRequest(
    val phoneNumber: String,
    val code: String,
    val deviceName: String? = null,
    val platform: String? = null,
    val osVersion: String? = null,
    val appVersion: String? = null
)

data class DeviceSessionDto(
    val id: String,
    val deviceName: String,
    val platform: String,
    val osVersion: String,
    val appVersion: String,
    val lastActiveIp: String,
    val lastActiveAt: String,
    val isCurrent: Boolean
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserDto? = null,
    val isNewUser: Boolean = false
)

data class RefreshTokenRequest(
    val refreshToken: String
)

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ‘¤ User DTOs
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class UserDto(
    val id: UUID,
    val username: String,
    val displayName: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val nationalCode: String? = null,
    val educationalRole: String? = null,
    val gradeLevel: String? = null,
    val major: String? = null,
    val phoneNumber: String?,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val isOnline: Boolean? = null,
    val lastSeen: Instant? = null,
    val points: Long = 0,
    val isContact: Boolean, // Added security field
    val profileVisibility: VisibilityOption = VisibilityOption.EVERYONE,
    val onlineVisibility: VisibilityOption = VisibilityOption.EVERYONE,
    val phoneVisibility: VisibilityOption = VisibilityOption.CONTACTS,
    // Profile Enhancements
    val university: String? = null,
    val fieldOfStudy: String? = null,
    val universities: List<String>? = emptyList(),
    val fieldsOfStudy: List<String>? = emptyList(),
    val isGraduated: Boolean = false,
    val education: String? = null,
    val skills: String? = null,
    val interests: String? = null,
    val workExperience: String? = null,
    val achievements: String? = null,
    // Feature 3: Bio channels (max 2)
    val bioChannelId1: UUID? = null,
    val bioChannelId2: UUID? = null,
    // Feature 4: Premium status
    val isPremium: Boolean = false,
    // Teacher role
    val isTeacher: Boolean = false,
    val teachingField: String? = null,
    val teachingUniversity: String? = null,
    // Location for targeting
    val province: String? = null,
    val city: String? = null,
    val faculty: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: java.time.LocalDate? = null,
    val institutionId: UUID? = null,
    val institutionLogoUrl: String? = null,
    val institutionName: String? = null,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val officialChannelId: UUID? = null
)

fun User.toDto(): UserDto = UserDto(
    id = id!!,
    username = username,
    displayName = displayName,
    firstName = firstName,
    lastName = lastName,
    nationalCode = nationalCode,
    educationalRole = educationalRole,
    gradeLevel = gradeLevel,
    major = major,
    phoneNumber = phoneNumber,
    avatarUrl = avatarUrl,
    bio = bio,
    isOnline = isOnline,
    lastSeen = lastSeen,
    points = points,
    isContact = true, // Own profile or full access context
    profileVisibility = profileVisibility ?: VisibilityOption.EVERYONE,
    onlineVisibility = onlineVisibility ?: VisibilityOption.EVERYONE,
    phoneVisibility = phoneVisibility ?: VisibilityOption.CONTACTS,
    university = profileDetails?.university,
    fieldOfStudy = profileDetails?.fieldOfStudy,
    universities = profileDetails?.universities?.toList() ?: emptyList(),
    fieldsOfStudy = profileDetails?.fieldsOfStudy?.toList() ?: emptyList(),
    isGraduated = profileDetails?.isGraduated ?: false,
    education = profileDetails?.education,
    skills = profileDetails?.skills,
    interests = profileDetails?.interests,
    workExperience = profileDetails?.workExperience,
    achievements = profileDetails?.achievements,
    bioChannelId1 = bioChannelId1,
    bioChannelId2 = bioChannelId2,
    isPremium = isPremium,
    isTeacher = profileDetails?.isTeacher ?: false,
    teachingField = profileDetails?.teachingField,
    teachingUniversity = profileDetails?.teachingUniversity,
    province = profileDetails?.province,
    city = profileDetails?.city,
    faculty = faculty,
    birthDate = birthDate,
    institutionId = institutionId,
    institutionLogoUrl = institutionLogoUrl,
    institutionName = institutionName,
    averageRating = averageRating,
    reviewCount = reviewCount,
    officialChannelId = officialChannelId
)

/**
 * Create a restricted UserDto based on viewer's relationship and target user's privacy settings.
 * Used when returning user info to other users.
 * NOTE: displayName is ALWAYS visible - it's a public identifier like username.
 * Privacy only affects avatar, phone, online status, and bio.
 */
fun User.toRestrictedDto(isContact: Boolean): UserDto {
    // ðŸ”’ SECURITY CRITICAL: Strict Privacy Enforcement ðŸ”’
    // This logic is the SOURCE OF TRUTH.
    // Explicitly determine phone visibility based on strict rules.
    
    val showPhone = when (phoneVisibility ?: VisibilityOption.CONTACTS) {
        VisibilityOption.EVERYONE -> true
        VisibilityOption.CONTACTS -> isContact // MUST be a contact to see
        VisibilityOption.NOBODY -> false
    }

    // DEBUG: Trace privacy decision in logs (can be removed later, keeping for verification)
    if (phoneVisibility == VisibilityOption.CONTACTS && !isContact) {
         println("PRIVACY_LOG: Hiding phone for user $id. Visibility=CONTACTS, isContact=$isContact, showPhone=$showPhone")
    }

    // Force null if not allowed. No exceptions.
    val finalPhoneNumber = if (showPhone) phoneNumber else null
    
    val showOnline = when (onlineVisibility ?: VisibilityOption.EVERYONE) {
        VisibilityOption.EVERYONE -> true
        VisibilityOption.CONTACTS -> isContact
        VisibilityOption.NOBODY -> false
    }
    
    val showProfile = when (profileVisibility ?: VisibilityOption.EVERYONE) {
        VisibilityOption.EVERYONE -> true
        VisibilityOption.CONTACTS -> isContact
        VisibilityOption.NOBODY -> false
    }
    
    return UserDto(
        id = id!!,
        username = username,
        displayName = displayName,
        firstName = firstName,
        lastName = lastName,
        nationalCode = nationalCode,
        educationalRole = educationalRole,
        gradeLevel = gradeLevel,
        major = major,
        phoneNumber = finalPhoneNumber,
        avatarUrl = if (showProfile) avatarUrl else null,
        bio = if (showProfile) bio else null,
        isOnline = if (showOnline) isOnline else null,
        lastSeen = if (showOnline) lastSeen else null,
        points = if (showProfile) points else 0,
        isContact = isContact, // Expose relationship to client for Sanitization Layer
        profileVisibility = profileVisibility ?: VisibilityOption.EVERYONE,
        onlineVisibility = onlineVisibility ?: VisibilityOption.EVERYONE,
        phoneVisibility = phoneVisibility ?: VisibilityOption.CONTACTS,
        university = if (showProfile) profileDetails?.university else null,
        fieldOfStudy = if (showProfile) profileDetails?.fieldOfStudy else null,
        universities = if (showProfile) profileDetails?.universities?.toList() ?: emptyList() else emptyList(),
        fieldsOfStudy = if (showProfile) profileDetails?.fieldsOfStudy?.toList() ?: emptyList() else emptyList(),
        isGraduated = profileDetails?.isGraduated ?: false,
        education = if (showProfile) profileDetails?.education else null,
        skills = if (showProfile) profileDetails?.skills else null,
        interests = if (showProfile) profileDetails?.interests else null,
        workExperience = if (showProfile) profileDetails?.workExperience else null,
        achievements = if (showProfile) profileDetails?.achievements else null,
        bioChannelId1 = if (showProfile) bioChannelId1 else null,
        bioChannelId2 = if (showProfile) bioChannelId2 else null,
        isPremium = isPremium,
        isTeacher = profileDetails?.isTeacher ?: false,
        teachingField = if (showProfile) profileDetails?.teachingField else null,
        teachingUniversity = if (showProfile) profileDetails?.teachingUniversity else null,
        province = if (showProfile) profileDetails?.province else null,
        city = if (showProfile) profileDetails?.city else null,
        faculty = faculty,
        birthDate = if (showProfile) birthDate else null,
        institutionId = if (showProfile) institutionId else null,
        institutionLogoUrl = if (showProfile) institutionLogoUrl else null,
        averageRating = averageRating,
        reviewCount = reviewCount,
        officialChannelId = officialChannelId
    )
}

// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 
// ðŸ   Home Dashboard DTOs
// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 

data class HomeDataResponse(
    val userCount: Long,
    val banners: List<HomeBannerDto>,
    val scienceEvents: List<ElmEventDto>,
    val movies: List<MovieDto>,
    val discounts: List<DiscountDto>,
    val universities: List<UniversityDto>
)

data class HomeBannerDto(
    val id: UUID,
    val title: String,
    val imageUrl: String,
    val linkUrl: String?,
    val colorStart: Long,
    val colorEnd: Long,
    val section: String = "HOME"
)

fun HomeBanner.toDto() = HomeBannerDto(
    id = id!!,
    title = title,
    imageUrl = imageUrl,
    linkUrl = linkUrl,
    colorStart = colorStart,
    colorEnd = colorEnd,
    section = section
)

data class DiscountDto(
    val id: UUID,
    val title: String,
    val brandName: String,
    val percent: Int,
    val code: String?,
    val imageUrl: String?
)

fun Discount.toDto() = DiscountDto(
    id = id!!,
    title = title,
    brandName = brandName,
    percent = percent,
    code = code,
    imageUrl = imageUrl
)

data class UniversityDto(
    val id: UUID,
    val name: String,
    val type: String?,
    val studentCount: Int,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String?,
    val rankings: String? = null
)

fun University.toDto() = UniversityDto(
    id = id!!,
    name = name,
    type = type,
    studentCount = studentCount,
    latitude = latitude,
    longitude = longitude,
    imageUrl = imageUrl,
    rankings = rankings
)

// Sub-DTOs for Entertainment (already used in EntertainmentService)
data class MovieDto(
    val id: UUID,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String?,
    val duration: String,
    val releaseDate: String
)

data class MusicDto(
    val id: UUID,
    val title: String,
    val artist: String,
    val audioUrl: String,
    val coverUrl: String?,
    val duration: String
)

data class GameChallengeDto(
    val id: UUID,
    val title: String,
    val description: String,
    val question: String,
    val reward: String,
    val type: String,
    val isMultipleChoice: Boolean = false,
    val options: List<RiddleOptionDto> = emptyList(),
    val correctAnswerIndex: Int? = null
)

data class RiddleOptionDto(
    val id: UUID,
    val text: String
)

data class EntertainmentResponse(
    val movies: List<MovieDto>,
    val music: List<MusicDto>,
    val challenges: List<GameChallengeDto>
)

data class RiddleResult(
    val success: Boolean,
    val message: String,
    val pointsAwarded: Long
)


// Sub-DTOs for ElmPeak (already used in ElmPeakService)
data class ElmPeakResponse(
    val featuredEvents: List<ElmEventDto>,
    val competitions: List<ElmEventDto>,
    val startups: List<ElmEventDto>,
    val congresses: List<ElmEventDto>
)

data class ElmEventDto(
    val id: UUID?,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val imageUrl: String?,
    val organizer: String?,
    val reward: String?,
    val type: ElmEventType,
    val isExternal: Boolean,
    val link: String?,
    val isApproved: Boolean = true
)

data class IdeaSubmissionRequest(
    val title: String,
    val description: String,
    val contactInfo: String
)

data class EventReportRequest(
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val link: String,
    val type: ElmEventType = ElmEventType.COMPETITION
)

// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 
// ðŸ’¬ Chat DTOs
// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 

data class ChatDto(
    val id: UUID,
    val type: ChatType,
    val title: String,
    val avatarUrl: String?,
    val lastMessage: MessageDto?,
    val unreadCount: Int,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val isArchived: Boolean,
    val participants: List<UserDto>,
    val updatedAt: Instant
)

data class CreateChatRequest(
    val participantId: UUID
)

data class ChatListResponse(
    val chats: List<ChatDto>,
    val totalCount: Int
)

// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 
// ðŸ“¨ Message DTOs
// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 

data class MessageDto(
    val id: UUID,
    val chatId: UUID,
    val senderId: UUID,
    val senderName: String,
    val senderAvatar: String?,
    val type: MessageType,
    val content: String,
    val mediaUrl: String?,
    val replyToMessageId: UUID?,
    val replyToMessage: MessageDto?,
    val forwardedFrom: String?,
    val status: MessageStatus,
    val isEdited: Boolean,
    val createdAt: Instant,
    val editedAt: Instant?,
    val reactions: Map<String, Int> = emptyMap(),
    val myReaction: String? = null,
    val poll: PollDto? = null,
    val amplitudes: List<Int>? = null,
    val isPinned: Boolean = false,
    val pinnedAt: Instant? = null,
    val scheduledAt: Instant? = null,
    val actionLabel: String? = null,
    val actionUrl: String? = null,
    val timerTargetAt: Instant? = null
)

data class ReactionRequest(
    val reaction: String?
)

data class SendMessageRequest(
    val type: MessageType = MessageType.TEXT,
    val content: String,
    val mediaUrl: String? = null,
    val replyToMessageId: UUID? = null,
    val pollId: UUID? = null,
    val amplitudes: List<Int>? = null
)

data class EditMessageRequest(
    val content: String
)

data class MessageListResponse(
    val messages: List<MessageDto>,
    val totalCount: Int,
    val hasMore: Boolean
)

fun Message.toDto(userId: UUID? = null): MessageDto {
    val showAvatar = if (userId != null && sender?.id == userId) {
        true
    } else {
        when (sender?.profileVisibility) {
            VisibilityOption.NOBODY -> false
            VisibilityOption.CONTACTS -> true // Assume visible in Private Chat
            else -> true
        }
    }
    
    return MessageDto(
        id = id!!,
        chatId = chat?.id ?: UUID.randomUUID(),
        senderId = sender?.id ?: UUID.randomUUID(),
        senderName = sender?.displayName ?: "",
        senderAvatar = if (showAvatar) sender?.avatarUrl else null,
        type = type,
        content = content,
        mediaUrl = mediaUrl,
        replyToMessageId = replyTo?.id,
        replyToMessage = replyTo?.toDto(userId),
        forwardedFrom = forwardedFrom,
        status = status,
        isEdited = isEdited,
        createdAt = createdAt,
        editedAt = editedAt,
        reactions = reactions.groupingBy { it.reaction }.eachCount(),
        myReaction = if (userId != null) reactions.find { it.user?.id == userId }?.reaction else null,
        poll = poll?.toDto(userId),
        amplitudes = amplitudes,
        isPinned = isPinned,
        pinnedAt = pinnedAt,
        scheduledAt = scheduledAt,
        actionLabel = actionLabel,
        actionUrl = actionUrl,
        timerTargetAt = timerTargetAt
    )
}

// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 
// ðŸ‘¥ Group DTOs
// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 

data class GroupDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val memberCount: Int,
    val isPublic: Boolean,
    val inviteLink: String?,
    val isInviteLinkEnabled: Boolean,
    val allowMembersToSendMessages: Boolean,
    val allowMembersToEditInfo: Boolean,
    val createdAt: Instant,
    val createdBy: UserDto?,
    val myRole: MemberRole?,
    val lastMessage: GroupMessageDto?,
    val unreadCount: Int,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val hideMembers: Boolean = false
)

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val memberIds: List<UUID> = emptyList(),
    val avatarUrl: String? = null,
    val targetProvince: String? = null,
    val targetCity: String? = null,
    val targetUniversity: String? = null,
    val targetFieldOfStudy: String? = null,
    val targetEducationLevel: String? = null
)

data class UpdateGroupRequest(
    val name: String?,
    val description: String?,
    val isPublic: Boolean?,
    val avatarUrl: String?
)

data class UpdateGroupSettingsRequest(
    val allowMembersToSendMessages: Boolean?,
    val allowMembersToEditInfo: Boolean?
)

data class InviteLinkResponse(
    val inviteLink: String?,
    val isEnabled: Boolean
)

data class GroupMemberDto(
    val user: UserDto,
    val role: MemberRole,
    val joinedAt: Instant,
    val canEditInfo: Boolean = false,
    val canPostStory: Boolean = false,
    val canAddMembers: Boolean = false,
    val canRemoveMembers: Boolean = false
)

data class AddGroupMembersRequest(
    val memberIds: List<UUID>
)

data class ChangeRoleRequest(
    val role: MemberRole,
    val canEditInfo: Boolean = false,
    val canPostStory: Boolean = false,
    val canAddMembers: Boolean = false,
    val canRemoveMembers: Boolean = false
)

data class GroupListResponse(
    val groups: List<GroupDto>,
    val totalCount: Int
)

data class GroupMessageListResponse(
    val messages: List<GroupMessageDto>,
    val totalCount: Int,
    val hasMore: Boolean
)

data class GroupMessageDto(
    val id: UUID,
    val groupId: UUID,
    val senderId: UUID,
    val senderName: String,
    val senderAvatar: String?,
    val type: MessageType,
    val content: String,
    val mediaUrl: String?,
    val replyToMessageId: UUID?,
    val replyToMessage: GroupMessageDto?,
    val forwardedFrom: String? = null,
    val isEdited: Boolean,
    val createdAt: Instant,
    val editedAt: Instant?,
    val reactions: Map<String, Int> = emptyMap(),
    val myReaction: String? = null,
    val poll: PollDto? = null,
    val amplitudes: List<Int>? = null,
    val isPinned: Boolean = false,
    val pinnedAt: Instant? = null,
    val scheduledAt: Instant? = null,
    val actionLabel: String? = null,
    val actionUrl: String? = null,
    val timerTargetAt: Instant? = null
)

data class SendGroupMessageRequest(
    val type: MessageType = MessageType.TEXT,
    val content: String,
    val mediaUrl: String? = null,
    val replyToMessageId: UUID? = null,
    val pollId: UUID? = null,
    val amplitudes: List<Int>? = null
)

fun GroupMessage.toDto(userId: UUID? = null): GroupMessageDto {
    val showAvatar = if (userId != null && sender?.id == userId) {
        true
    } else {
        when (sender?.profileVisibility) {
            VisibilityOption.NOBODY -> false
            VisibilityOption.CONTACTS -> false // Default hidden in Groups for safety
            else -> true
        }
    }

    return GroupMessageDto(
        id = id!!,
        groupId = group?.id ?: UUID.randomUUID(),
        senderId = sender?.id ?: UUID.randomUUID(),
        senderName = sender?.displayName ?: "",
        senderAvatar = if (showAvatar) sender?.avatarUrl else null,
        type = type,
        content = content,
        mediaUrl = mediaUrl,
        replyToMessageId = replyTo?.id,
        replyToMessage = replyTo?.toDto(userId),
        forwardedFrom = forwardedFrom,
        isEdited = isEdited,
        createdAt = createdAt,
        editedAt = editedAt,
        reactions = reactions.groupingBy { it.reaction }.eachCount(),
        myReaction = if (userId != null) reactions.find { it.user?.id == userId }?.reaction else null,
        poll = poll?.toDto(userId),
        amplitudes = amplitudes,
        isPinned = isPinned,
        pinnedAt = pinnedAt,
        scheduledAt = scheduledAt,
        actionLabel = actionLabel,
        actionUrl = actionUrl,
        timerTargetAt = timerTargetAt
    )
}

fun GroupMessage.toMessageDto(userId: UUID? = null): MessageDto = MessageDto(
    id = id!!,
    chatId = group?.id ?: UUID.randomUUID(),
    senderId = sender?.id ?: UUID.randomUUID(),
    senderName = sender?.displayName ?: "",
    senderAvatar = if (userId != null && sender?.id == userId) sender?.avatarUrl else null,
    type = type,
    content = content,
    mediaUrl = mediaUrl,
    replyToMessageId = replyTo?.id,
    replyToMessage = null, // Simplify for shared media
    forwardedFrom = null,
    status = MessageStatus.SENT,
    isEdited = isEdited,
    createdAt = createdAt,
    editedAt = editedAt,
    reactions = reactions.groupingBy { it.reaction }.eachCount(),
    myReaction = if (userId != null) reactions.find { it.user?.id == userId }?.reaction else null,
    poll = poll?.toDto(userId),
    amplitudes = amplitudes
)

// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 
// ðŸ“¢ Channel DTOs
// â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• â• 

data class ChannelDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val subscriberCount: Int,
    val isPublic: Boolean,
    val publicId: String?, // Unique public identifier (e.g., "mychannel")
    val inviteLink: String?,
    val owner: UserDto?,
    val isSubscribed: Boolean,
    val isAdmin: Boolean,
    val lastPost: ChannelPostDto?,
    val unreadCount: Int,
    val createdAt: Instant,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)

data class CreateChannelRequest(
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = true,
    val publicId: String? = null, // Optional unique public identifier
    val memberIds: List<UUID> = emptyList(),
    val avatarUrl: String? = null,
    val targetProvince: String? = null,
    val targetCity: String? = null,
    val targetUniversity: String? = null,
    val targetFieldOfStudy: String? = null,
    val targetEducationLevel: String? = null
)

data class UpdateChannelRequest(
    val name: String?,
    val description: String?,
    val isPublic: Boolean?,
    val publicId: String?, // Unique public identifier
    val avatarUrl: String?
)

data class ChannelListResponse(
    val channels: List<ChannelDto>,
    val totalCount: Int
)

data class ChannelPostDto(
    val id: UUID,
    val channelId: UUID,
    val type: MessageType,
    val content: String,
    val mediaUrl: String?,
    val viewCount: Int,
    val commentsEnabled: Boolean,
    val createdAt: Instant,
    val editedAt: Instant?,
    val poll: PollDto? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val amplitudes: List<Int>? = null,
    val isPinned: Boolean = false,
    val pinnedAt: Instant? = null,
    val forwardedFrom: String? = null,
    val scheduledAt: Instant? = null,
    val actionLabel: String? = null,
    val actionUrl: String? = null,
    val timerTargetAt: Instant? = null,
    val isAd: Boolean = false,
    val adLabel: String? = null,
    val adSourceChannelId: UUID? = null
)

data class CreatePostRequest(
    val content: String,
    val mediaUrl: String? = null,
    val commentsEnabled: Boolean = true,
    val type: String = "TEXT",
    val pollId: UUID? = null,
    val amplitudes: List<Int>? = null
)

data class CreateCommentRequest(
    val content: String
)

data class CommentListResponse(
    val comments: List<ChannelPostCommentDto>,
    val totalCount: Int,
    val hasMore: Boolean
)

data class ChannelPostCommentDto(
    val id: UUID,
    val user: UserDto,
    val content: String,
    val createdAt: Instant
)

data class UpdateUserRequest(
    val username: String? = null,
    val displayName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val nationalCode: String? = null,
    val educationalRole: String? = null,
    val gradeLevel: String? = null,
    val major: String? = null,
    val faculty: String? = null,
    val bio: String? = null,
    val university: String? = null,
    val fieldOfStudy: String? = null,
    val universities: List<String>? = null,
    val fieldsOfStudy: List<String>? = null,
    val isGraduated: Boolean? = null,
    val education: String? = null,
    val skills: String? = null,
    val interests: String? = null,
    val workExperience: String? = null,
    val achievements: String? = null,
    val bioChannelId1: UUID? = null,
    val bioChannelId2: UUID? = null,
    val isTeacher: Boolean? = null,
    val teachingField: String? = null,
    val teachingUniversity: String? = null,
    val province: String? = null,
    val city: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: java.time.LocalDate? = null
)

data class UserSearchResult(
    val users: List<UserDto>,
    val totalCount: Int
)

data class UpdatePrivacyRequest(
    val profileVisibility: VisibilityOption?,
    val onlineVisibility: VisibilityOption?,
    val phoneVisibility: VisibilityOption?
)

fun ChannelPostComment.toDto(): ChannelPostCommentDto = ChannelPostCommentDto(
    id = id!!,
    user = user?.toDto() ?: UserDto(
        id = UUID.randomUUID(),
        username = "Unknown",
        displayName = "Unknown",
        phoneNumber = null,
        avatarUrl = null,
        bio = null,
        isOnline = false,
        lastSeen = null,
        isContact = false
    ),
    content = content,
    createdAt = createdAt
)

data class PostListResponse(
    val posts: List<ChannelPostDto>,
    val totalCount: Int,
    val hasMore: Boolean
)

fun ChannelPost.toDto(userId: UUID? = null): ChannelPostDto = ChannelPostDto(
    id = id!!,
    channelId = channel?.id ?: UUID.randomUUID(),
    type = type,
    content = content,
    mediaUrl = mediaUrl,
    viewCount = viewCount,
    commentsEnabled = commentsEnabled,
    createdAt = createdAt,
    editedAt = editedAt,
    poll = poll?.toDto(userId),
    reactions = reactions.groupingBy { it.reaction }.eachCount(),
    amplitudes = amplitudes,
    isPinned = isPinned,
    pinnedAt = pinnedAt,
    forwardedFrom = forwardedFrom,
    scheduledAt = scheduledAt,
    actionLabel = actionLabel,
    actionUrl = actionUrl,
    timerTargetAt = timerTargetAt,
    isAd = isAd,
    adLabel = adLabel,
    adSourceChannelId = adSourceChannelId
)

fun ChannelPost.toMessageDto(userId: UUID? = null): MessageDto = MessageDto(
    id = id!!,
    chatId = channel?.id ?: UUID.randomUUID(),
    senderId = channel?.owner?.id ?: UUID.randomUUID(), // Channels usually don't have a sender per post, using owner
    senderName = channel?.name ?: "",
    senderAvatar = channel?.avatarUrl,
    type = type,
    content = content,
    mediaUrl = mediaUrl,
    replyToMessageId = null,
    replyToMessage = null,
    forwardedFrom = null,
    status = MessageStatus.SENT,
    isEdited = false,
    createdAt = createdAt,
    editedAt = editedAt,
    reactions = reactions.groupingBy { it.reaction }.eachCount(),
    myReaction = null,
    poll = poll?.toDto(userId),
    amplitudes = amplitudes
)

data class ChannelSubscriberDto(
    val user: UserDto,
    val isAdmin: Boolean,
    val joinedAt: Instant
)

data class ChannelAdminRequest(
    val userId: UUID
)

data class AddChannelMembersRequest(
    val memberIds: List<UUID>
)

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ”„ Common Response
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

data class ErrorResponse(
    val success: Boolean = false,
    val message: String,
    val errorCode: String? = null
)

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ“¸ Story DTOs
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class StoryDto(
    val id: UUID,
    val userId: UUID?,
    val userDisplayName: String,
    val userAvatarUrl: String?,
    // Feature 7: Channel/Group stories
    val channelId: UUID? = null,
    val channelName: String? = null,
    val groupId: UUID? = null,
    val groupName: String? = null,
    val mediaUrl: String,
    val type: StoryType,
    val caption: String?,
    val duration: Int,
    val createdAt: Instant,
    val expiresAt: Instant,
    val isViewed: Boolean,
    val viewCount: Int
)

data class StoryViewDto(
    val userId: UUID,
    val userDisplayName: String,
    val userAvatarUrl: String?,
    val viewedAt: Instant
)

data class StoryUserDto(
    val userId: UUID,
    val username: String,
    val displayName: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val nationalCode: String? = null,
    val educationalRole: String? = null,
    val gradeLevel: String? = null,
    val major: String? = null,
    val avatarUrl: String?,
    val bio: String?,
    val stories: List<StoryDto>,
    val isCurrentUser: Boolean = false
)

// Feature 7: Channel/Group story wrapper
data class StorySourceDto(
    val id: UUID,
    val name: String,
    val avatarUrl: String?,
    val sourceType: String, // "USER", "CHANNEL", "GROUP"
    val stories: List<StoryDto>
)

fun Story.toDto(currentUserId: UUID?): StoryDto {
    return StoryDto(
        id = id!!,
        userId = user?.id,
        userDisplayName = user?.displayName ?: channel?.name ?: group?.name ?: "",
        userAvatarUrl = user?.avatarUrl ?: channel?.avatarUrl ?: group?.avatarUrl,
        channelId = channel?.id,
        channelName = channel?.name,
        groupId = group?.id,
        groupName = group?.name,
        mediaUrl = mediaUrl,
        type = type,
        caption = caption,
        duration = durationSeconds,
        createdAt = createdAt,
        expiresAt = expiresAt,
        isViewed = if (currentUserId != null) views.any { it.user?.id == currentUserId } else false,
        viewCount = views.distinctBy { it.user?.id }.size
    )
}

fun StoryView.toDto(): StoryViewDto {
    return StoryViewDto(
        userId = user?.id!!,
        userDisplayName = user?.displayName ?: "",
        userAvatarUrl = user?.avatarUrl,
        viewedAt = viewedAt
    )
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ“Š Poll DTOs
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class PollDto(
    val id: UUID,
    val question: String,
    val isMultipleChoice: Boolean,
    val isAnonymous: Boolean,
    val options: List<PollOptionDto>,
    val totalVotes: Int,
    val userVotedOptionIds: List<UUID> = emptyList(),
    val createdAt: Instant
)

data class PollOptionDto(
    val id: UUID,
    val text: String,
    val voteCount: Int,
    val votePercentage: Float
)

data class CreatePollRequest(
    val question: String,
    val options: List<String>,
    val isMultipleChoice: Boolean = false,
    val isAnonymous: Boolean = false
)

data class VoteRequest(
    val optionIds: List<UUID>
)

fun Poll.toDto(currentUserId: UUID?): PollDto {
    val total = options.sumOf { it.voteCount }
    val userVotes = if (currentUserId != null) {
        votes.filter { it.user?.id == currentUserId }.mapNotNull { it.option?.id }
    } else emptyList()
    
    return PollDto(
        id = id!!,
        question = question,
        isMultipleChoice = isMultipleChoice,
        isAnonymous = isAnonymous,
        options = options.map { 
            PollOptionDto(
                id = it.id!!,
                text = it.text,
                voteCount = it.voteCount,
                votePercentage = if (total > 0) (it.voteCount.toFloat() / total) * 100 else 0f
            ) 
        },
        totalVotes = total,
        userVotedOptionIds = userVotes,
        createdAt = createdAt
    )
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ“‹ Profile Details DTOs
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class ProfileDetailsDto(
    val userId: UUID,
    val university: String?,
    val fieldOfStudy: String?,
    val education: String?,
    val interests: List<String>,
    val achievements: List<String>,
    val skills: List<String>,
    val workExperience: String?,
    val isTeacher: Boolean = false,
    val teachingField: String? = null,
    val teachingUniversity: String? = null,
    val province: String? = null,
    val city: String? = null,
    
    // Academy Profile
    val academyName: String? = null,
    val academyHashtags: List<String> = emptyList(),
    val faculty: String? = null,
    val updatedAt: Instant?
)

data class UpdateProfileDetailsRequest(
    val university: String?,
    val fieldOfStudy: String?,
    val universities: List<String>? = null,
    val fieldsOfStudy: List<String>? = null,
    val isGraduated: Boolean? = null,
    val education: String?,
    val faculty: String?,
    val interests: List<String>?,
    val achievements: List<String>?,
    val skills: List<String>?,
    val workExperience: String?,
    val isTeacher: Boolean? = null,
    val teachingField: String? = null,
    val teachingUniversity: String? = null,
    val province: String? = null,
    val city: String? = null,
    val academyName: String? = null,
    val academyHashtags: List<String>? = null
)

fun UserProfileDetails.toDto(): ProfileDetailsDto {
    val interestsList = interests?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    val achievementsList = achievements?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    val skillsList = skills?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    val academyHashtagsList = academyHashtags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    
    return ProfileDetailsDto(
        userId = user?.id!!,
        university = university,
        fieldOfStudy = fieldOfStudy,
        education = education,
        interests = interestsList,
        achievements = achievementsList,
        skills = skillsList,
        workExperience = workExperience,
        isTeacher = isTeacher,
        teachingField = teachingField,
        teachingUniversity = teachingUniversity,
        province = province,
        city = city,
        academyName = academyName,
        academyHashtags = academyHashtagsList,
        faculty = faculty,
        updatedAt = updatedAt
    )
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ‘¥ Follow System DTOs
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class FollowDto(
    val id: UUID,
    val user: UserDto,
    val status: FollowStatus,
    val createdAt: Instant
)

data class FollowUserRequest(
    val userId: UUID
)

data class FollowListResponse(
    val users: List<FollowDto>,
    val totalCount: Int,
    val hasMore: Boolean
)

data class FollowCountsDto(
    val followerCount: Int,
    val followingCount: Int
)

fun UserFollow.toFollowerDto(): FollowDto = FollowDto(
    id = id!!,
    user = follower!!.toDto(),
    status = status,
    createdAt = createdAt
)

fun UserFollow.toFollowingDto(): FollowDto = FollowDto(
    id = id!!,
    user = following!!.toDto(),
    status = status,
    createdAt = createdAt
)

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ¤ Collaboration DTOs
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class CollaborationRequestDto(
    val id: UUID,
    val sender: UserDto,
    val receiver: UserDto,
    val title: String,
    val message: String,
    val status: CollaborationStatus,
    val createdAt: Instant,
    val respondedAt: Instant?
)

data class SendCollaborationRequest(
    val receiverId: UUID,
    val title: String,
    val message: String
)

data class CollaborationListResponse(
    val requests: List<CollaborationRequestDto>,
    val totalCount: Int,
    val hasMore: Boolean
)

fun CollaborationRequest.toDto(): CollaborationRequestDto = CollaborationRequestDto(
    id = id!!,
    sender = sender!!.toDto(),
    receiver = receiver!!.toDto(),
    title = title,
    message = message,
    status = status,
    createdAt = createdAt,
    respondedAt = respondedAt
)

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ”” Notification DTOs
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class NotificationDto(
    val id: UUID,
    val type: NotificationType,
    val title: String,
    val body: String,
    val relatedEntityId: UUID?,
    val actorId: UUID?,
    val actorName: String?,
    val actorAvatarUrl: String?,
    val isRead: Boolean,
    val createdAt: Instant,
    val status: String = "PENDING"
)

data class NotificationListResponse(
    val notifications: List<NotificationDto>,
    val totalCount: Int,
    val unreadCount: Int,
    val hasMore: Boolean
)

data class UnreadCountResponse(
    val unreadCount: Int
)

fun Notification.toDto(): NotificationDto = NotificationDto(
    id = id!!,
    type = type,
    title = title,
    body = body,
    relatedEntityId = relatedEntityId,
    actorId = actorId,
    actorName = actorName,
    actorAvatarUrl = actorAvatarUrl,
    isRead = isRead,
    createdAt = createdAt,
    status = status
)

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ‘¤ Extended User DTO (with social data)
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class UserWithSocialDto(
    val id: UUID,
    val username: String,
    val displayName: String,
    val phoneNumber: String?,
    val avatarUrl: String?,
    val bio: String?,
    val isOnline: Boolean?,
    val lastSeen: Instant?,
    val isContact: Boolean,
    val followerCount: Int,
    val followingCount: Int,
    val isFollowedByMe: Boolean,
    val isFollowingMe: Boolean,
    val profileDetails: ProfileDetailsDto?
)

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ“Œ Pin / Forward / Schedule DTOs
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class PinMessageRequest(
    val isPinned: Boolean
)

data class ForwardMessageRequest(
    val messageIds: List<UUID>,
    val targetChatId: UUID? = null,
    val targetGroupId: UUID? = null,
    val targetChannelId: UUID? = null,
    val targetType: String // "CHAT", "GROUP", "CHANNEL"
)

data class ScheduleMessageRequest(
    val type: MessageType = MessageType.TEXT,
    val content: String,
    val mediaUrl: String? = null,
    val scheduledAt: Instant,
    val amplitudes: List<Int>? = null
)

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ’¬ Story Reply DTOs
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class StoryReplyRequest(
    val content: String
)

data class StoryReplyDto(
    val id: UUID,
    val storyId: UUID,
    val userId: UUID,
    val userDisplayName: String,
    val userAvatarUrl: String?,
    val content: String,
    val createdAt: Instant
)

fun StoryReply.toDto(): StoryReplyDto = StoryReplyDto(
    id = id!!,
    storyId = story?.id ?: UUID.randomUUID(),
    userId = user?.id ?: UUID.randomUUID(),
    userDisplayName = user?.displayName ?: "",
    userAvatarUrl = user?.avatarUrl,
    content = content,
    createdAt = createdAt
)

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ‘¤ Username Setting DTO
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class SetUsernameRequest(
    val username: String
)

data class UsernameAvailabilityResponse(
    val username: String,
    val isAvailable: Boolean
)

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ðŸ“š Reference Data & Ad DTOs
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

data class FieldOfStudyDto(
    val id: String,
    val name: String,
    val educationLevel: String
)

fun FieldOfStudy.toDto(): FieldOfStudyDto = FieldOfStudyDto(
    id = id.toString(),
    name = name,
    educationLevel = educationLevel
)

data class EducationLevelDto(
    val id: String,
    val name: String,
    val roleValueEn: String? = null,
    val hasFieldOfStudy: Boolean = false,
    val hasFaculty: Boolean = false
)

fun EducationLevel.toDto(): EducationLevelDto = EducationLevelDto(
    id = id.toString(),
    name = name,
    roleValueEn = roleValueEn,
    hasFieldOfStudy = hasFieldOfStudy,
    hasFaculty = hasFaculty
)

data class FacultyDto(
    val id: String,
    val name: String,
    val educationLevel: String? = null
)

fun Faculty.toDto(): FacultyDto = FacultyDto(
    id = id.toString(),
    name = name,
    educationLevel = educationLevel
)

data class UniversitySimpleDto(
    val id: String,
    val name: String,
    val city: String?,
    val province: String?
)

fun University.toSimpleDto(): UniversitySimpleDto = UniversitySimpleDto(
    id = id.toString(),
    name = name,
    city = city,
    province = province
)

data class EducationalRoleOptionDto(
    val id: String,
    val labelFa: String,
    val valueEn: String,
    val emoji: String
)

fun EducationalRoleOption.toDto(): EducationalRoleOptionDto = EducationalRoleOptionDto(
    id = id.toString(),
    labelFa = labelFa,
    valueEn = valueEn,
    emoji = emoji
)

data class ClubDto(
    val id: String,
    val name: String
)

fun Club.toDto() = ClubDto(
    id = id.toString(),
    name = name
)

data class StudentOrgDto(
    val id: String,
    val name: String
)

fun StudentOrg.toDto() = StudentOrgDto(
    id = id.toString(),
    name = name
)

data class ReferenceDataDto(
    val universities: List<UniversitySimpleDto>,
    val fieldsOfStudy: List<FieldOfStudyDto>,
    val educationLevels: List<EducationLevelDto>,
    val faculties: List<FacultyDto>,
    val educationalRoles: List<EducationalRoleOptionDto> = emptyList(),
    val clubs: List<ClubDto> = emptyList(),
    val studentOrgs: List<StudentOrgDto> = emptyList()
)

data class InstitutionResponse(
    val id: UUID,
    val name: String,
    val type: String,
    val registrationNumber: String?,
    val contactPhone: String?,
    val contactEmail: String?,
    val province: String?,
    val city: String?,
    val address: String?,
    val logoUrl: String?,
    val description: String?,
    val isSubsidiary: Boolean,
    val dependencyDescription: String?,
    val universities: List<String>,
    val specialties: List<String>,
    val associatedClubIds: List<String>,
    val associatedFieldOfStudyIds: List<String>,
    val associatedStudentOrgIds: List<String>,
    val instructorIds: List<UUID>,
    val manualInstructors: List<ManualInstructorDto> = emptyList(),
    val adminIds: List<UUID>,
    val ownerId: UUID,
    val channelId: UUID?,
    val verificationStatus: String,
    val adminNote: String?,
    val isActive: Boolean,
    val createdAt: Instant,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val courseCount: Int = 0,
    val studentCount: Int = 0,
    val totalTrainingHours: Int = 0,
    val totalPersonHours: Int = 0,
    val totalTeachersCount: Int = 0,
    val totalCollaborations: Int = 0,
    val totalRevenue: Long? = null,
    val totalViews: Long = 0,
    val totalClicks: Long = 0,
    val rating: Double = 0.0,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val honors: List<InstitutionHonorDto> = emptyList()
)

fun Institution.toResponse() = InstitutionResponse(
    id = id!!,
    name = name,
    type = type.name,
    registrationNumber = registrationNumber,
    contactPhone = contactPhone,
    contactEmail = contactEmail,
    province = province,
    city = city,
    address = address,
    logoUrl = logoUrl,
    description = description,
    isSubsidiary = isSubsidiary,
    dependencyDescription = dependencyDescription,
    universities = universities,
    specialties = specialties,
    associatedClubIds = associatedClubIds,
    associatedFieldOfStudyIds = associatedFieldOfStudyIds,
    associatedStudentOrgIds = associatedStudentOrgIds,
    instructorIds = instructorIds,
    manualInstructors = manualInstructors.map { ManualInstructorDto(it.name, it.avatarUrl, it.resume) },
    adminIds = adminIds,
    ownerId = owner?.id ?: UUID.randomUUID(),
    channelId = channel?.id,
    verificationStatus = verificationStatus.name,
    adminNote = adminNote,
    isActive = isActive,
    createdAt = createdAt,
    averageRating = averageRating,
    reviewCount = reviewCount,
    honors = honors.map { it.toDto() },
    courseCount = 0,
    studentCount = 0,
    totalTrainingHours = 0,
    totalPersonHours = 0,
    totalTeachersCount = 0,
    totalCollaborations = 0,
    totalRevenue = null,
    totalViews = 0,
    totalClicks = 0
)

data class InstitutionHonorDto(
    val id: UUID,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: java.time.LocalDate?
)

fun InstitutionHonor.toDto() = InstitutionHonorDto(
    id = id!!,
    title = title,
    description = description,
    imageUrl = imageUrl,
    date = date
)

data class MosbatElmHomeDataDto(
    val banners: List<HomeBannerDto>,
    val featuredInstitutions: List<InstitutionResponse>,
    val popularInstitutions: List<InstitutionResponse> = emptyList(),
    val upcomingCourses: List<CourseDto>,
    val popularTeachers: List<UserDto> = emptyList(),
    val categories: List<String> = listOf("student_orgs", "clubs", "universities", "institutions", "general", "special", "discounted", "all")
)

data class AiBotMessageDto(
    val id: String,
    val botId: String,
    val content: String,
    val role: String,
    val actionLabel: String? = null,
    val actionUrl: String? = null,
    val createdAt: String
)

fun AiBotMessage.toDto(): AiBotMessageDto = AiBotMessageDto(
    id = id.toString(),
    botId = botId.toString(),
    content = content,
    role = role,
    actionLabel = actionLabel,
    actionUrl = actionUrl,
    createdAt = createdAt.toString()
)

data class SendAiBotMessageRequest(
    val content: String
)

data class AdRequestDto(
    val id: UUID,
    val requesterId: UUID,
    val requesterName: String,
    val requesterAvatar: String?,
    val sourceMessageId: String,
    val sourceType: String,
    val sourceId: String?,
    val targetChannelId: UUID,
    val targetChannelName: String,
    val targetChannelAvatar: String?,
    val messageContent: String,
    val messageMediaUrl: String?,
    val messageType: String,
    val status: String,
    val createdAt: Instant,
    val reviewedAt: Instant?
)

data class CreateAdRequest(
    val messageContent: String,
    val targetChannelId: UUID,
    val sourceType: String = "CHAT",
    val sourceId: String? = null,
    val sourceMessageId: String = "",
    val messageMediaUrl: String? = null,
    val messageType: String = "TEXT"
)

data class AdRequestListResponse(
    val adRequests: List<AdRequestDto>,
    val totalCount: Int
)

fun AdRequest.toDto(): AdRequestDto = AdRequestDto(
    id = id!!,
    requesterId = requester?.id ?: UUID.randomUUID(),
    requesterName = requester?.displayName ?: "Unknown",
    requesterAvatar = requester?.avatarUrl,
    sourceMessageId = sourceMessageId,
    sourceType = sourceType,
    sourceId = sourceId,
    targetChannelId = targetChannel?.id ?: UUID.randomUUID(),
    targetChannelName = targetChannel?.name ?: "Unknown",
    targetChannelAvatar = targetChannel?.avatarUrl,
    messageContent = messageContent,
    messageMediaUrl = messageMediaUrl,
    messageType = messageType.name,
    status = status.name,
    createdAt = createdAt,
    reviewedAt = reviewedAt
)

// ═══════════════════════════════════════════════════════════════════════════════
// ⭐ Special Folder DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class SpecialFolderDto(
    val aiBots: List<AiBotDto>,
    val channels: List<SpecialChannelDto>,
    val groups: List<SpecialGroupDto>,
    val supportChannels: List<SpecialChannelDto> = emptyList(),
    val supportGroups: List<SpecialGroupDto> = emptyList(),
    val supportChatId: String?,
    val isProfileComplete: Boolean = true
)

data class AiBotDto(
    val id: String,
    val name: String,
    val botType: String,
    val category: String,
    val description: String?,
    val avatarUrl: String?
)

fun AiBot.toDto(): AiBotDto = AiBotDto(
    id = id.toString(),
    name = name,
    botType = botType,
    category = category,
    description = description,
    avatarUrl = avatarUrl
)

data class SpecialChannelDto(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val category: String,
    val subscriberCount: Int
)

data class SpecialGroupDto(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val category: String,
    val memberCount: Int
)

data class CreateAiBotRequest(
    val name: String,
    val botType: String,
    val description: String? = null,
    val avatarUrl: String? = null
)

data class CreateOfficialGroupRequest(
    val name: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val category: OfficialGroupCategory,
    val hideMembers: Boolean = false,
    val displayMode: String = "SPECIAL",
    val targetFieldOfStudy: String? = null,
    val targetEducationLevel: String? = null,
    val targetProvince: String? = null,
    val targetCity: String? = null,
    val targetUniversity: String? = null,
    val targetMinistry: String? = null,
    val targetAudienceType: String? = null,
    val adminIds: List<String>? = null
)

data class CreateOfficialChannelRequest(
    val name: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val category: OfficialChannelCategory,
    val displayMode: String = "SPECIAL",
    val targetFieldOfStudy: String? = null,
    val targetEducationLevel: String? = null,
    val targetProvince: String? = null,
    val targetCity: String? = null,
    val targetUniversity: String? = null,
    val targetMinistry: String? = null,
    val targetAudienceType: String? = null,
    val adminIds: List<String>? = null
)

data class AddOfficialAdminRequest(
    val userId: UUID
)

data class OfficialChannelAdminDto(
    val id: String,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val category: String,
    val subscriberCount: Int,
    val admins: List<UserDto>,
    val displayMode: String = "SPECIAL",
    val targetFieldOfStudy: String? = null,
    val targetEducationLevel: String? = null,
    val targetProvince: String? = null,
    val targetCity: String? = null,
    val targetUniversity: String? = null,
    val targetMinistry: String? = null,
    val targetAudienceType: String? = null
)

data class OfficialGroupAdminDto(
    val id: String,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val category: String,
    val hideMembers: Boolean,
    val memberCount: Int,
    val admins: List<UserDto>,
    val displayMode: String = "SPECIAL",
    val targetFieldOfStudy: String? = null,
    val targetEducationLevel: String? = null,
    val targetProvince: String? = null,
    val targetCity: String? = null,
    val targetUniversity: String? = null,
    val targetMinistry: String? = null,
    val targetAudienceType: String? = null
)


// ═══════════════════════════════════════════════════════════════════════════════
// 📚 Course & Academy DTOs (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

data class CourseChapterDto(
    val title: String,
    val durationText: String,
    val sessionStartTime: Instant? = null,
    val sessionEndTime: Instant? = null
)

data class ManualInstructorDto(
    val name: String,
    val avatarUrl: String? = null,
    val resume: String? = null
)

data class CourseDto(

    val id: UUID,
    val title: String,
    val slogan: String?,
    val description: String?,
    val coverImageUrl: String?,
    val organizerId: UUID?,
    val institutionId: UUID?,
    val organizerName: String?,
    val organizerAvatarUrl: String?,
    val organizerDescription: String?,
    val scientificAssociationName: String?,
    val priceRials: Long,
    val capacity: Int?,
    val enrollmentLimit: Int?,
    val enrolledCount: Long = 0,
    val fieldOfStudy: String? = null,
    val educationLevel: String? = null,
    val channelId: UUID? = null,
    val groupId: UUID? = null,
    val startsAt: Instant,
    val endsAt: Instant,
    val tags: List<String>,
    val suitableFor: List<String>,
    val chapters: List<CourseChapterDto>,
        val teachers: List<UserDto>,
    val manualInstructors: List<ManualInstructorDto> = emptyList(),

    val admins: List<UserDto>,
    val favoritesCount: Int,
    val isPublic: Boolean,
    val status: String,
    val adminNote: String? = null,
    val discountPercentage: Int = 0,
    val syllabusDuration: String? = null,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val viewCount: Long = 0,
    val clickCount: Long = 0,
    val isVerticalPoster: Boolean = false,
    val hasOnlineClass: Boolean = false,
    val organizerType: String? = null,
    val managerId: UUID,
    val managerName: String,
    val managerAvatarUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class CreateCourseRequest(
    val title: String,
    val slogan: String? = null,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val priceRials: Long = 0,
    val capacity: Int? = null,
    val enrollmentLimit: Int? = null,
    val discountPercentage: Int? = null,
    val syllabusDuration: String? = null,
    val fieldOfStudy: String? = null,
    val educationLevel: String? = null,
    val startsAt: Instant,
    val endsAt: Instant? = null,
    val tags: List<String> = emptyList(),
    val suitableFor: List<String> = emptyList(),
    val chapters: List<CourseChapterDto> = emptyList(),
    val collaborators: List<String> = emptyList(),
        val teacherIds: List<UUID> = emptyList(),
    val manualInstructors: List<ManualInstructorDto> = emptyList(),

    val adminIds: List<UUID> = emptyList(),
    val organizerDescription: String? = null,
    val scientificAssociationName: String? = null,
    val channelIds: List<UUID> = emptyList(),
    val isPublic: Boolean = true
)

fun Course.toDto(organizerType: String? = null): CourseDto {
    // Resolve organizer identity: prefer Institution (academy) over messaging profile
    val resolvedName: String? = organizer?.institutionName ?: organizer?.displayName
    val resolvedAvatar: String? = organizer?.institutionLogoUrl ?: organizer?.avatarUrl
    return CourseDto(
    id = id!!,
    title = title,
    slogan = slogan,
    description = description,
    coverImageUrl = coverImageUrl,
    organizerId = organizer?.id,
    institutionId = institutionId,
    organizerName = resolvedName,
    organizerAvatarUrl = resolvedAvatar,
    organizerDescription = organizerDescription,
    scientificAssociationName = scientificAssociationName,
    priceRials = priceRials,
    capacity = capacity,
    enrollmentLimit = enrollmentLimit,
    fieldOfStudy = fieldOfStudy,
    educationLevel = educationLevel,
    channelId = channel?.id,
    groupId = group?.id,
    startsAt = startsAt,
    endsAt = endsAt,
    tags = tags,
    suitableFor = suitableFor,
    chapters = chapters.map { CourseChapterDto(it.title, it.durationText, it.sessionStartTime) },
        teachers = teachers.map { it.toDto() },
    manualInstructors = manualInstructors.map { ManualInstructorDto(it.name, it.avatarUrl, it.resume) },

    admins = admins.map { it.toDto() },
    favoritesCount = favoritesCount,
    isPublic = isPublic,
    status = status.name,
    adminNote = adminNote,
    discountPercentage = discountPercentage,
    syllabusDuration = syllabusDuration,
    averageRating = averageRating,
    reviewCount = reviewCount,
    viewCount = viewCount,
    clickCount = clickCount,
    isVerticalPoster = isVerticalPoster,
    hasOnlineClass = bbbMeetingId != null,
    organizerType = organizerType,
    managerId = organizer?.id ?: java.util.UUID.randomUUID(),
    managerName = organizer?.displayName ?: "Unknown",
    managerAvatarUrl = organizer?.avatarUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🤝 Course Collaboration DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class CourseCollaborationRequestDto(
    val id: UUID,
    val courseId: UUID,
    val courseTitle: String,
    val senderInstitutionId: UUID,
    val senderInstitutionName: String,
    val targetInstitutionId: UUID,
    val status: String,
    val message: String?,
    val createdAt: Instant
)

data class CreateCollaborationRequest(
    val targetInstitutionId: UUID,
    val message: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💡 Feedback DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class CreateFeedbackRequest(
    val title: String,
    val description: String,
    val rating: Int
)

data class FeedbackResponse(
    val id: UUID,
    val userId: UUID?,
    val userDisplayName: String?,
    val title: String,
    val description: String,
    val rating: Int,
    val status: String,
    val createdAt: Instant,
    val adminNote: String?
)

data class UpdateFeedbackStatusRequest(
    val status: String,
    val adminNote: String? = null
)

