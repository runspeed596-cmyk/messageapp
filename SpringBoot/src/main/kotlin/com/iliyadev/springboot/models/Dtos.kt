package com.iliyadev.springboot.models

import java.time.Instant
import java.util.UUID
import com.iliyadev.springboot.models.Poll

// ═══════════════════════════════════════════════════════════════════════════════
// 🔐 Auth DTOs
// ═══════════════════════════════════════════════════════════════════════════════

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
    val code: String
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

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 User DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class UserDto(
    val id: UUID,
    val username: String,
    val displayName: String,
    val phoneNumber: String?,
    val avatarUrl: String?,
    val bio: String?,
    val isOnline: Boolean?,
    val lastSeen: Instant?,
    val points: Long = 0,
    val isContact: Boolean, // Added security field
    val profileVisibility: VisibilityOption = VisibilityOption.EVERYONE,
    val onlineVisibility: VisibilityOption = VisibilityOption.EVERYONE,
    val phoneVisibility: VisibilityOption = VisibilityOption.CONTACTS,
    // Profile Enhancements
    val university: String? = null,
    val fieldOfStudy: String? = null,
    val education: String? = null,
    val skills: String? = null,
    val interests: String? = null,
    val workExperience: String? = null,
    val achievements: String? = null,
    // Feature 3: Bio channels (max 2)
    val bioChannelId1: UUID? = null,
    val bioChannelId2: UUID? = null,
    // Feature 4: Premium status
    val isPremium: Boolean = false
)

fun User.toDto(): UserDto = UserDto(
    id = id!!,
    username = username,
    displayName = displayName,
    phoneNumber = phoneNumber,
    avatarUrl = avatarUrl,
    bio = bio,
    isOnline = isOnline,
    lastSeen = lastSeen,
    points = points,
    isContact = true, // Own profile or full access context
    profileVisibility = profileVisibility,
    onlineVisibility = onlineVisibility,
    phoneVisibility = phoneVisibility,
    university = profileDetails?.university,
    fieldOfStudy = profileDetails?.fieldOfStudy,
    education = profileDetails?.education,
    skills = profileDetails?.skills,
    interests = profileDetails?.interests,
    workExperience = profileDetails?.workExperience,
    achievements = profileDetails?.achievements,
    bioChannelId1 = bioChannelId1,
    bioChannelId2 = bioChannelId2,
    isPremium = isPremium
)

/**
 * Create a restricted UserDto based on viewer's relationship and target user's privacy settings.
 * Used when returning user info to other users.
 * NOTE: displayName is ALWAYS visible - it's a public identifier like username.
 * Privacy only affects avatar, phone, online status, and bio.
 */
fun User.toRestrictedDto(isContact: Boolean): UserDto {
    // 🔒 SECURITY CRITICAL: Strict Privacy Enforcement 🔒
    // This logic is the SOURCE OF TRUTH.
    // Explicitly determine phone visibility based on strict rules.
    
    val showPhone = when (phoneVisibility) {
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
    
    val showOnline = when (onlineVisibility) {
        VisibilityOption.EVERYONE -> true
        VisibilityOption.CONTACTS -> isContact
        VisibilityOption.NOBODY -> false
    }
    
    val showProfile = when (profileVisibility) {
        VisibilityOption.EVERYONE -> true
        VisibilityOption.CONTACTS -> isContact
        VisibilityOption.NOBODY -> false
    }
    
    return UserDto(
        id = id!!,
        username = username,
        displayName = displayName, // ALWAYS visible - not affected by profile privacy
        phoneNumber = finalPhoneNumber,
        avatarUrl = if (showProfile) avatarUrl else null,
        bio = if (showProfile) bio else null,
        isOnline = if (showOnline) isOnline else null,
        lastSeen = if (showOnline) lastSeen else null,
        points = if (showProfile) points else 0,
        isContact = isContact, // Expose relationship to client for Sanitization Layer
        profileVisibility = profileVisibility,
        onlineVisibility = onlineVisibility,
        phoneVisibility = phoneVisibility,
        university = if (showProfile) profileDetails?.university else null,
        fieldOfStudy = if (showProfile) profileDetails?.fieldOfStudy else null,
        education = if (showProfile) profileDetails?.education else null,
        skills = if (showProfile) profileDetails?.skills else null,
        interests = if (showProfile) profileDetails?.interests else null,
        workExperience = if (showProfile) profileDetails?.workExperience else null,
        achievements = if (showProfile) profileDetails?.achievements else null,
        bioChannelId1 = if (showProfile) bioChannelId1 else null,
        bioChannelId2 = if (showProfile) bioChannelId2 else null,
        isPremium = isPremium
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🏠 Home Dashboard DTOs
// ═══════════════════════════════════════════════════════════════════════════════

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
    val colorEnd: Long
)

fun HomeBanner.toDto() = HomeBannerDto(
    id = id!!,
    title = title,
    imageUrl = imageUrl,
    linkUrl = linkUrl,
    colorStart = colorStart,
    colorEnd = colorEnd
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
    val imageUrl: String?
)

fun University.toDto() = UniversityDto(
    id = id!!,
    name = name,
    type = type,
    studentCount = studentCount,
    latitude = latitude,
    longitude = longitude,
    imageUrl = imageUrl
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
    val text: String,
    val displayOrder: Int
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

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat DTOs
// ═══════════════════════════════════════════════════════════════════════════════

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

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Message DTOs
// ═══════════════════════════════════════════════════════════════════════════════

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
    val amplitudes: List<Int>? = null
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
        amplitudes = amplitudes
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group DTOs
// ═══════════════════════════════════════════════════════════════════════════════

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
    val isArchived: Boolean = false
)

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val memberIds: List<UUID> = emptyList(),
    val avatarUrl: String? = null
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
    val isEdited: Boolean,
    val createdAt: Instant,
    val editedAt: Instant?,
    val reactions: Map<String, Int> = emptyMap(),
    val myReaction: String? = null,
    val poll: PollDto? = null,
    val amplitudes: List<Int>? = null
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
        isEdited = isEdited,
        createdAt = createdAt,
        editedAt = editedAt,
        reactions = reactions.groupingBy { it.reaction }.eachCount(),
        myReaction = if (userId != null) reactions.find { it.user?.id == userId }?.reaction else null,
        poll = poll?.toDto(userId),
        amplitudes = amplitudes
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel DTOs
// ═══════════════════════════════════════════════════════════════════════════════

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
    val avatarUrl: String? = null
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
    val amplitudes: List<Int>? = null
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
    val username: String?,
    val displayName: String?,
    val bio: String?,
    val university: String?,
    val fieldOfStudy: String?,
    val education: String?,
    val skills: String?,
    val interests: String?,
    val workExperience: String?,
    val achievements: String?,
    val bioChannelId1: UUID?,
    val bioChannelId2: UUID?
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
    user = user?.toDto() ?: UserDto(UUID.randomUUID(), "Unknown", "Unknown", null, null, null, false, null, isContact = false),
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

// ═══════════════════════════════════════════════════════════════════════════════
// 🔄 Common Response
// ═══════════════════════════════════════════════════════════════════════════════

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

// ═══════════════════════════════════════════════════════════════════════════════
// 📸 Story DTOs
// ═══════════════════════════════════════════════════════════════════════════════

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
    val avatarUrl: String?,
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
        viewCount = views.size
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

// ═══════════════════════════════════════════════════════════════════════════════
// 📊 Poll DTOs
// ═══════════════════════════════════════════════════════════════════════════════

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

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Profile Details DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class ProfileDetailsDto(
    val userId: UUID,
    val university: String?,
    val fieldOfStudy: String?,
    val education: String?,
    val interests: List<String>,
    val achievements: List<String>,
    val skills: List<String>,
    val workExperience: String?,
    val updatedAt: Instant?
)

data class UpdateProfileDetailsRequest(
    val university: String?,
    val fieldOfStudy: String?,
    val education: String?,
    val interests: List<String>?,
    val achievements: List<String>?,
    val skills: List<String>?,
    val workExperience: String?
)

fun UserProfileDetails.toDto(): ProfileDetailsDto {
    val interestsList = interests?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    val achievementsList = achievements?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    val skillsList = skills?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    
    return ProfileDetailsDto(
        userId = user?.id!!,
        university = university,
        fieldOfStudy = fieldOfStudy,
        education = education,
        interests = interestsList,
        achievements = achievementsList,
        skills = skillsList,
        workExperience = workExperience,
        updatedAt = updatedAt
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Follow System DTOs
// ═══════════════════════════════════════════════════════════════════════════════

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

// ═══════════════════════════════════════════════════════════════════════════════
// 🤝 Collaboration DTOs
// ═══════════════════════════════════════════════════════════════════════════════

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

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Notification DTOs
// ═══════════════════════════════════════════════════════════════════════════════

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
    val createdAt: Instant
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
    createdAt = createdAt
)

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 Extended User DTO (with social data)
// ═══════════════════════════════════════════════════════════════════════════════

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



