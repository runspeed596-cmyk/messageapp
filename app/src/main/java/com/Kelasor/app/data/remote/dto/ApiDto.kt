package com.Kelasor.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════════════════════════════
// 📦 Generic Response Wrapper
// ═══════════════════════════════════════════════════════════════════════════════

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val size: Int = 20,
    val hasNext: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 Auth & User DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class SendOtpRequest(val phoneNumber: String)
data class SendOtpResponse(
    val success: Boolean,
    val message: String,
    val expiresInSeconds: Int = 0 // Int to match Repository
)

data class VerifyOtpRequest(
    val phoneNumber: String,
    val code: String,
    val deviceName: String? = null,
    val platform: String? = null,
    val osVersion: String? = null,
    val appVersion: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("accessToken", alternate = ["access_token"])
    val accessToken: String? = null,
    @SerializedName("refreshToken", alternate = ["refresh_token"])
    val refreshToken: String? = null,
    val user: UserDto? = null,
    val isNewUser: Boolean = false,
    val expiresInSeconds: Int = 0 // Int to match Repository
)

data class RefreshTokenRequest(val refreshToken: String)

data class UserDto(
    val id: String,
    val phoneNumber: String? = null/*PRIVACY: null when hidden by backend*/,
    val username: String = "",
    val displayName: String = "",
    val bio: String? = null,
    val avatarUrl: String? = null,
    val lastSeen: String? = null,
    val isOnline: Boolean = false,
    val isContact: Boolean? = false,

    // New Mosbat Elm Fields
    val firstName: String? = null,
    val lastName: String? = null,
    val nationalCode: String? = null,
    val educationalRole: String? = null,
    val gradeLevel: String? = null,
    val major: String? = null,
    val faculty: String? = null,
    val birthDate: String? = null,

    // Profile Details
    val university: String? = null,
    val fieldOfStudy: String? = null,
    val universities: List<String>? = null,
    val fieldsOfStudy: List<String>? = null,
    val isGraduated: Boolean = false,
    val education: String? = null,
    val skills: String? = null,
    val interests: String? = null,
    val workExperience: String? = null,
    val achievements: String? = null,

    // Feature 3: Bio channels (max 2)
    val bioChannelId1: String? = null,
    val bioChannelId2: String? = null,
    
    // Feature 4: Premium status
    val isPremium: Boolean = false,

    // Teacher role
    val isTeacher: Boolean = false,
    val teachingField: String? = null,
    val teachingUniversity: String? = null,
    // Location for targeting
    val province: String? = null,
    val city: String? = null,

    // Privacy Settings
    val profileVisibility: String? = null,
    val onlineVisibility: String? = null,
    val phoneVisibility: String? = null,
    val institutionId: String? = null,
    val institutionLogoUrl: String? = null,
    val institutionName: String? = null,
    val role: String? = null,

    // Rating & Channel fields (from server)
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val officialChannelId: String? = null
)

// Positional args must match UserRepository usage
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
    val birthDate: String? = null,
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
    val avatarUrl: String? = null,
    // Feature 3: Bio channels
    val bioChannelId1: String? = null,
    val bioChannelId2: String? = null,
    // Teacher role
    val isTeacher: Boolean? = null,
    val teachingField: String? = null,
    val teachingUniversity: String? = null,
    val province: String? = null,
    val city: String? = null
)

data class UpdatePrivacyRequest(
    val onlineVisibility: String? = null,
    val profileVisibility: String? = null,
    val phoneVisibility: String? = null
)

data class UserSearchResult(
    val users: List<UserDto>
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class ChatDto(
    val id: String,
    val type: String,
    val title: String = "",
    val avatarUrl: String? = null,
    val participants: List<UserDto> = emptyList(),
    val lastMessage: MessageDto? = null,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ChatListResponse(
    val chats: List<ChatDto>
)

data class CreateChatRequest(
    val participantId: String
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Message DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class MessageDto(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String = "",
    val senderAvatar: String? = null,
    val content: String,
    val type: String = "TEXT",
    val mediaUrl: String? = null,
    val status: String = "SENT",
    val replyToMessageId: String? = null,
    val replyToMessage: MessageDto? = null,
    val forwardedFrom: String? = null,
    val isEdited: Boolean = false,
    val createdAt: String? = null,
    val editedAt: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val myReaction: String? = null,
    val poll: PollDto? = null,
    val amplitudes: List<Int>? = null,
    val isPinned: Boolean = false,
    val pinnedAt: String? = null,
    val scheduledAt: String? = null,
    val actionLabel: String? = null,
    val actionUrl: String? = null,
    val timerTargetAt: String? = null
)

data class MessageListResponse(
    val messages: List<MessageDto>
)

data class SendMessageRequest(
    val content: String,
    val type: String = "TEXT",
    val mediaUrl: String? = null,
    val replyToMessageId: String? = null,
    val pollId: String? = null,
    val amplitudes: List<Int>? = null
)

data class EditMessageRequest(
    val content: String
)

data class ReactionRequest(
    val reaction: String?
)

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class GroupDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val createdBy: UserDto? = null,
    val memberCount: Int = 0,
    val isPublic: Boolean = false,
    val inviteLink: String? = null,
    val isInviteLinkEnabled: Boolean = true,
    val createdAt: String? = null,
    val allowMembersToSendMessages: Boolean = true,
    val allowMembersToEditInfo: Boolean = false,
    val myRole: String? = null,
    val lastMessage: MessageDto? = null,

    val unreadCount: Int = 0,
    // Sync settings
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val hideMembers: Boolean = false
)

data class GroupListResponse(
    val groups: List<GroupDto>
)

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val memberIds: List<String> = emptyList(),
    val avatarUrl: String? = null,
    val targetProvince: String? = null,
    val targetCity: String? = null,
    val targetUniversity: String? = null,
    val targetFieldOfStudy: String? = null,
    val targetEducationLevel: String? = null
)

data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null,
    val isPublic: Boolean? = null,
    val avatarUrl: String? = null
)

data class GroupMemberDto(
    val user: UserDto,
    val role: String,
    val joinedAt: String? = null,
    val canEditInfo: Boolean = false,
    val canPostStory: Boolean = false,
    val canAddMembers: Boolean = false,
    val canRemoveMembers: Boolean = false
)

data class AddGroupMembersRequest(
    val memberIds: List<String>
)

data class ChangeRoleRequest(
    val role: String,
    val canEditInfo: Boolean = false,
    val canPostStory: Boolean = false,
    val canAddMembers: Boolean = false,
    val canRemoveMembers: Boolean = false
)

data class GroupMessageDto(
    val id: String? = null,
    val groupId: String? = null,
    val senderId: String? = null,
    val senderName: String? = "",
    val senderAvatar: String? = null,
    val content: String? = "",
    val type: String? = "TEXT",
    val mediaUrl: String? = null,
    val status: String? = "SENT",
    val replyToMessageId: String? = null,
    val replyToMessage: GroupMessageDto? = null,
    val forwardedFrom: String? = null,
    val isEdited: Boolean = false,
    val createdAt: String? = null,
    val editedAt: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val myReaction: String? = null,
    val poll: PollDto? = null,
    val amplitudes: List<Int>? = null,
    val isPinned: Boolean = false,
    val pinnedAt: String? = null,
    val scheduledAt: String? = null,
    val actionLabel: String? = null,
    val actionUrl: String? = null,
    val timerTargetAt: String? = null
)

data class GroupMessageListResponse(
    val messages: List<GroupMessageDto>
)

data class SendGroupMessageRequest(
    val type: String = "TEXT",
    val content: String,
    val mediaUrl: String? = null,
    val replyToMessageId: String? = null,
    val pollId: String? = null,
    val amplitudes: List<Int>? = null
)

data class UpdateGroupSettingsRequest(
    val allowMembersToSendMessages: Boolean? = null,
    val allowMembersToEditInfo: Boolean? = null
)

data class InviteLinkResponse(
    val inviteLink: String,
    val inviteCode: String
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class ChannelDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val owner: UserDto? = null,
    val subscriberCount: Int = 0,
    val isPublic: Boolean = true,
    val publicId: String? = null,
    val inviteLink: String? = null,
    val createdAt: String? = null,
    val isAdmin: Boolean = false,
    val isSubscribed: Boolean = false,
    val chatType: String = "CHANNEL",
    val lastPost: ChannelPostDto? = null,

    val unreadCount: Int = 0,
    // Sync settings
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)

data class ChannelListResponse(
    val channels: List<ChannelDto>
)

data class CreateChannelRequest(
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = true,
    val publicId: String? = null,
    val memberIds: List<String> = emptyList(),
    val targetProvince: String? = null,
    val targetCity: String? = null,
    val targetUniversity: String? = null,
    val targetFieldOfStudy: String? = null,
    val targetEducationLevel: String? = null
)

data class UpdateChannelRequest(
    val name: String? = null,
    val description: String? = null,
    val isPublic: Boolean? = null,
    val publicId: String? = null,
    val avatarUrl: String? = null
)

data class ChannelPostDto(
    val id: String? = null,
    val channelId: String? = null,
    val content: String? = "",
    val type: String? = "TEXT",
    val mediaUrl: String? = null,
    val viewCount: Int = 0,
    val commentsEnabled: Boolean = true,
    val commentCount: Int = 0,
    val createdAt: String? = null,
    val editedAt: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val poll: PollDto? = null,
    val amplitudes: List<Int>? = null,
    val myReaction: String? = null,
    val isPinned: Boolean = false,
    val pinnedAt: String? = null,
    val scheduledAt: String? = null,
    val forwardedFrom: String? = null,
    val isEdited: Boolean = false,
    val isAd: Boolean = false,
    val adLabel: String? = null,
    val adSourceChannelId: String? = null,
    val actionLabel: String? = null,
    val actionUrl: String? = null,
    val timerTargetAt: String? = null
)

data class PostListResponse(
    val posts: List<ChannelPostDto>
)

data class CreatePostRequest(
    val content: String,
    val mediaUrl: String? = null,
    val commentsEnabled: Boolean = true,
    val type: String = "TEXT",
    val pollId: String? = null,
    val amplitudes: List<Int>? = null
)

data class ChannelPostCommentDto(
    val id: String,
    val postId: String,
    val user: UserDto,
    val content: String,
    val createdAt: String = ""
)

data class CommentListResponse(
    val comments: List<ChannelPostCommentDto>
)

data class CreateCommentRequest(
    val content: String
)

data class ChannelAdminRequest(
    val userId: String
)

data class AddChannelMembersRequest(
    val memberIds: List<String>
)

data class ChannelSubscriberDto(
    val user: UserDto,
    val isAdmin: Boolean = false,
    val joinedAt: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📊 Poll DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class PollDto(
    val id: String,
    val question: String,
    val options: List<PollOptionDto>,
    val isMultipleChoice: Boolean = false,
    val isAnonymous: Boolean = false,
    val expiresAt: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val totalVotes: Int = 0,
    val userVotedOptionIds: List<String> = emptyList()
)

data class PollOptionDto(
    val id: String,
    val text: String,
    val voteCount: Int = 0,
    val votePercentage: Float = 0f
)

data class CreatePollRequest(
    val question: String,
    val options: List<String>,
    val allowMultipleAnswers: Boolean = false,
    val isAnonymous: Boolean = false,
    val expiresInMinutes: Int? = null
)

data class VoteRequest(
    val optionIds: List<String>
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Profile & Follow DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class ProfileDetailsDto(
    val userId: String,
    val university: String? = null,
    val fieldOfStudy: String? = null,
    val education: String? = null,
    val skills: String? = null,
    val interests: String? = null,
    val workExperience: String? = null,
    val achievements: String? = null,
    val province: String? = null,
    val city: String? = null
)

data class UpdateProfileDetailsRequest(
    val university: String? = null,
    val fieldOfStudy: String? = null,
    val education: String? = null,
    val skills: String? = null,
    val interests: String? = null,
    val workExperience: String? = null,
    val achievements: String? = null,
    val province: String? = null,
    val city: String? = null
)

data class FollowDto(
    val user: UserDto,
    val followedAt: Long = 0
)

data class FollowListResponse(
    val users: List<FollowDto>,
    val hasMore: Boolean = false
)

data class FollowCountsDto(
    val followerCount: Int,
    val followingCount: Int
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🤝 Collaboration DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class SendCollaborationRequest(
    val receiverId: String,
    val title: String,
    val message: String,
    val projectType: String? = null
)

data class CollaborationRequestDto(
    val id: String,
    val sender: UserDto,
    val receiver: UserDto,
    val title: String,
    val description: String,
    val status: String,
    val projectType: String? = null,
    val createdAt: String? = null
)

data class CollaborationListResponse(
    val requests: List<CollaborationRequestDto>
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Notification DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class NotificationDto(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val relatedId: String? = null,
    val isRead: Boolean = false,
    val createdAt: String = "",
    val actorId: String? = null,
    val actorName: String? = null,
    val actorAvatarUrl: String? = null
)

data class NotificationListResponse(
    val notifications: List<NotificationDto>,
    val hasMore: Boolean = false,
    val unreadCount: Int = 0
)

data class UnreadCountResponse(
    val unreadCount: Int // Renamed to match ViewModel
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🎮 Entertainment DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class MovieDto(
    val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val duration: String? = null,
    val releaseDate: String? = null
)

data class MusicDto(
    val id: String,
    val title: String,
    val artist: String,
    val audioUrl: String,
    val albumArtUrl: String? = null,
    val duration: String? = null
)

data class GameChallengeDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val question: String,
    val reward: String? = null,
    val type: String? = null,
    val isMultipleChoice: Boolean = false,
    val options: List<RiddleOptionDto> = emptyList(),
    val correctAnswerIndex: Int? = null
)

data class RiddleOptionDto(
    val id: String,
    val text: String,
    val displayOrder: Int
)

data class RiddleResultDto(
    val success: Boolean,
    val message: String
)

data class SolveRiddleRequest(
    val riddleId: String,
    val optionIndex: Int
)

data class EntertainmentResponse(
    val movies: List<MovieDto>,
    val music: List<MusicDto>,
    val challenges: List<GameChallengeDto>
)

// ═══════════════════════════════════════════════════════════════════════════════
// ⛰️ Elm Peak DTOs
// ═══════════════════════════════════════════════════════════════════════════════

enum class ElmEventType {
    COMPETITION, STARTUP, CONGRESS
}

data class ElmEventDto(
    val id: String?,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val imageUrl: String? = null,
    val organizer: String? = null,
    val reward: String? = null,
    val type: ElmEventType,
    val isExternal: Boolean = false,
    val link: String? = null
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

data class ElmPeakResponse(
    val featuredEvents: List<ElmEventDto>,
    val competitions: List<ElmEventDto>,
    val startups: List<ElmEventDto>,
    val congresses: List<ElmEventDto>
)

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
    val id: String,
    val title: String,
    val imageUrl: String,
    val linkUrl: String? = null,
    val colorStart: Long,
    val colorEnd: Long
)

data class DiscountDto(
    val id: String,
    val title: String,
    val brandName: String,
    val percent: Int,
    val code: String? = null,
    val imageUrl: String? = null
)

data class UniversityDto(
    val id: String,
    val name: String,
    val country: String? = null,
    val province: String? = null,
    val city: String? = null,
    val ministryName: String? = null,
    val type: String? = null,
    val establishedYear: Int? = null,
    val studentCount: Int = 0,
    val iranRank: Int? = null,
    val worldRank: Int? = null,
    val articleCount: Int = 0,
    val journalCount: Int = 0,
    val facilities: String? = null,
    val faculties: String? = null,
    val departments: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String? = null,
    val websiteUrl: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📌 Pin / Forward / Schedule DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class PinMessageRequest(
    val isPinned: Boolean
)

data class ForwardMessageRequest(
    val messageIds: List<String>,
    val targetChatId: String? = null,
    val targetGroupId: String? = null,
    val targetChannelId: String? = null,
    val targetType: String // "CHAT", "GROUP", "CHANNEL"
)

data class ScheduleMessageRequest(
    val content: String,
    val type: String = "TEXT",
    val mediaUrl: String? = null,
    val scheduledAt: String
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🆔 Username DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class SetUsernameRequest(
    val username: String
)

data class UsernameAvailabilityResponse(
    val isAvailable: Boolean,
    val message: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// ⭐ Special Folder DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class SpecialFolderDto(
    val aiBots: List<AiBotDto> = emptyList(),
    val channels: List<SpecialChannelDto> = emptyList(),
    val groups: List<SpecialGroupDto> = emptyList(),
    val supportChannels: List<SpecialChannelDto> = emptyList(),
    val supportGroups: List<SpecialGroupDto> = emptyList(),
    val supportChatId: String? = null,
    val isProfileComplete: Boolean = true
)

data class AiBotDto(
    val id: String,
    val name: String,
    val botType: String,
    val category: String = "GENERAL",
    val description: String? = null,
    val avatarUrl: String? = null,
    val displayOrder: Int = 0
)

data class SpecialChannelDto(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val category: String = "",
    val subscriberCount: Int = 0
)

data class SpecialGroupDto(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val category: String = "",
    val memberCount: Int = 0
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📚 Reference Data DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class FieldOfStudyDto(
    val id: String,
    val name: String,
    val educationLevel: String = "",
    val displayOrder: Int = 0
)

data class EducationLevelDto(
    val id: String,
    val name: String,
    val roleValueEn: String? = null,
    val displayOrder: Int = 0,
    val hasFaculty: Boolean = false,
    val hasFieldOfStudy: Boolean = false
)

data class FacultyDto(
    val id: String,
    val name: String,
    val educationLevel: String? = null,
    val displayOrder: Int = 0
)

data class UniversitySimpleDto(
    val id: String,
    val name: String,
    val city: String? = null,
    val province: String? = null
)

data class EducationalRoleOptionDto(
    val id: String,
    val labelFa: String,
    val valueEn: String,
    val emoji: String = "",
    val displayOrder: Int = 0
)

data class ClubDto(
    val id: String,
    val name: String,
    val displayOrder: Int
)

data class StudentOrgDto(
    val id: String,
    val name: String,
    val displayOrder: Int
)

data class ReferenceDataDto(
    val universities: List<UniversitySimpleDto> = emptyList(),
    val fieldsOfStudy: List<FieldOfStudyDto> = emptyList(),
    val faculties: List<FacultyDto> = emptyList(),
    val educationLevels: List<EducationLevelDto> = emptyList(),
    val educationalRoles: List<EducationalRoleOptionDto> = emptyList(),
    val clubs: List<ClubDto> = emptyList(),
    val studentOrgs: List<StudentOrgDto> = emptyList()
)

data class BannerDto(
    val id: String,
    val title: String,
    val imageUrl: String,
    val linkUrl: String? = null,
    val colorStart: Long = 0,
    val colorEnd: Long = 0,
    val section: String = "HOME"
)

data class MosbatElmHomeDataDto(
    val banners: List<BannerDto> = emptyList(),
    val featuredInstitutions: List<InstitutionDto> = emptyList(),
    val popularInstitutions: List<InstitutionDto> = emptyList(),
    val upcomingCourses: List<CourseDto> = emptyList(),
    val popularTeachers: List<UserDto> = emptyList(),
    val categories: List<String> = emptyList()
)

data class InstitutionDto(
    val id: String,
    val name: String,
    val type: String,
    val logoUrl: String? = null,
    val description: String? = null,
    val isSubsidiary: Boolean = false,
    val dependencyDescription: String? = null,
    val province: String? = null,
    val city: String? = null,
    val verificationStatus: String = "PENDING_VERIFICATION",
    val channelId: String? = null,
    val ownerId: String,
    val isActive: Boolean = false,
    val universities: List<String>? = emptyList(),
    val faculties: List<String>? = emptyList(),
    val specialties: List<String>? = emptyList(),
    val achievements: String? = null,
    val associatedClubIds: List<String>? = emptyList(),
    val associatedFieldOfStudyIds: List<String>? = emptyList(),
    val associatedStudentOrgIds: List<String>? = emptyList(),
    val instructorIds: List<String>? = emptyList(),
    val manualInstructors: List<ManualInstructorDto>? = emptyList(),
    val adminIds: List<String>? = emptyList(),
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val courseCount: Int = 0,
    val studentCount: Int = 0,
    val totalTrainingHours: Int = 0,
    val totalPersonHours: Int = 0,
    val totalTeachersCount: Int = 0,
    val totalCollaborations: Int = 0,
    val totalRevenue: Long? = null,
    val rating: Double = 0.0,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val honors: List<InstitutionHonorDto> = emptyList()
)

data class InstitutionHonorDto(
    val id: String? = null,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val date: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🤖 AI Bot Chat DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class AiBotMessageDto(
    val id: String,
    val botId: String,
    val content: String,
    val role: String, // USER or ASSISTANT
    val actionLabel: String? = null,
    val actionUrl: String? = null,
    val createdAt: String
)

data class SendAiBotMessageRequest(
    val content: String
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📁 Smart Folder DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class SmartFolderDto(
    val folderType: String, // TEACHERS, ELM_CLUB, COURSES, PURCHASED
    val labelFa: String,
    val iconName: String?,
    val channels: List<SmartFolderChannelDto> = emptyList()
)

data class SmartFolderChannelDto(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val subscriberCount: Long = 0,
    val isVerifiedTeacher: Boolean = false,
    val classification: String = "GENERAL",
    val isSubscribed: Boolean = false,
    val chatType: String = "CHANNEL",
    val lastMessage: String? = null,
    val unreadCount: Int = 0,
    val createdAt: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📚 Course DTOs (for Courses folder)
// ═══════════════════════════════════════════════════════════════════════════════

data class ManualInstructorDto(
    val name: String,
    val avatarUrl: String? = null,
    val resume: String? = null
)

data class EnrollmentRequestDto(
    val paymentType: String // WALLET, ONLINE
)

data class EnrollmentResponseDto(
    val id: String,
    val courseId: String,
    val courseTitle: String,
    val userId: String,
    val enrolledAt: String,
    val isActive: Boolean,
    val course: CourseDto? = null
)

data class CourseDto(
    val id: String,
    val title: String,
    val slogan: String? = null,
    val description: String? = null,
    val favoritesCount: Int = 0,
    val teachers: List<UserDto> = emptyList(),
    val admins: List<UserDto> = emptyList(),
    val organizerId: String,
    val organizerName: String? = null,
    val organizerAvatarUrl: String? = null,
    val organizerDescription: String? = null,
    val scientificAssociationName: String? = null,
    val institutionId: String? = null,
    val channelId: String? = null,
    val groupId: String? = null,
    val coverImageUrl: String? = null,
    val fieldOfStudy: String? = null,
    val educationLevel: String? = null,
    val startsAt: String,
    val endsAt: String,
    val enrollmentLimit: Int? = null,
    val capacity: Int? = null,
    val enrolledCount: Long = 0,
    val isPublic: Boolean = true,
    val status: String = "DRAFT",
    val adminNote: String? = null,
    val priceRials: Long = 0,
    val tags: List<String> = emptyList(),
    val suitableFor: List<String> = emptyList(),
    val chapters: List<CourseChapterDto> = emptyList(),
    val manualInstructors: List<ManualInstructorDto> = emptyList(),
    val isVerticalPoster: Boolean = false,
    val discountPercentage: Int? = null,
    val syllabusDuration: String? = null,
    val organizerType: String? = null,
    val managerId: String,
    val managerName: String,
    val managerAvatarUrl: String? = null,
    val createdAt: String,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val hasOnlineClass: Boolean = false
)

data class CourseChapterDto(
    val title: String,
    val durationText: String,
    val sessionStartTime: String? = null,
    val sessionEndTime: String? = null
)

data class CreateCourseRequest(
    val title: String,
    val slogan: String? = null,
    val description: String? = null,
    val adminIds: List<String> = emptyList(),
    val teacherIds: List<String> = emptyList(),
    val institutionId: String? = null,
    val coverImageUrl: String? = null,
    val fieldOfStudy: String? = null,
    val educationLevel: String? = null,
    val startsAt: String,
    val endsAt: String,
    val enrollmentLimit: Int? = null,
    val capacity: Int? = null,
    val discountPercentage: Int? = null,
    val syllabusDuration: String? = null,
    val collaborators: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val priceRials: Long = 0,
    val tags: List<String> = emptyList(),
    val suitableFor: List<String> = emptyList(),
    val chapters: List<CourseChapterDto> = emptyList(),
    val manualInstructors: List<ManualInstructorDto> = emptyList(),
    val organizerDescription: String? = null,
    val scientificAssociationName: String? = null,
    val channelIds: List<String> = emptyList(),
    val isVerticalPoster: Boolean = false
)

data class CourseReviewRequestDto(
    val status: String,
    val adminNote: String? = null
)

data class CourseCommentDto(
    val id: String,
    val courseId: String,
    val userId: String,
    val userDisplayName: String,
    val userAvatarUrl: String? = null,
    val content: String,
    val rating: Int = 0,
    val replyToCommentId: String? = null,
    val createdAt: String? = null
)

data class AddCourseCommentRequest(
    val content: String,
    val rating: Int = 0,
    val replyToCommentId: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🤝 Collaboration DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class CourseCollaborationRequestDto(
    val id: String,
    val courseId: String,
    val courseTitle: String,
    val senderInstitutionId: String,
    val senderInstitutionName: String,
    val targetInstitutionId: String,
    val status: String,
    val message: String? = null,
    val createdAt: String
)

data class CreateCollaborationRequest(
    val targetInstitutionId: String,
    val message: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🏛️ Institution DTOs
// ═══════════════════════════════════════════════════════════════════════════════


data class InstitutionRegisterRequestDto(
    val name: String,
    val type: String,
    val registrationNumber: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val province: String? = null,
    val city: String? = null,
    val address: String? = null,
    val logoUrl: String? = null,
    val description: String? = null,
    val isSubsidiary: Boolean = false,
    val dependencyDescription: String? = null,
    val universities: List<String> = emptyList(),
    val specialties: List<String> = emptyList(),
    val associatedClubIds: List<String> = emptyList(),
    val associatedFieldOfStudyIds: List<String> = emptyList(),
    val associatedStudentOrgIds: List<String> = emptyList(),
    val instructorIds: List<String> = emptyList(),
    val manualInstructors: List<ManualInstructorDto> = emptyList(),
    val adminIds: List<String> = emptyList()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Exam DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class ExamDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val creatorId: String,
    val courseId: String? = null,
    val channelId: String? = null,
    val startsAt: String,
    val endsAt: String,
    val durationMinutes: Int = 60,
    val totalScore: Double = 0.0,
    val passScore: Double? = null,
    val status: String = "DRAFT",
    val isPublic: Boolean = false,
    val shuffleQuestions: Boolean = false,
    val shuffleOptions: Boolean = false,
    val showResultsAfter: Boolean = true,
    val maxAttempts: Int = 1,
    val questionCount: Long = 0,
    val attemptCount: Long = 0,
    val createdAt: String? = null
)

data class ExamQuestionDto(
    val id: String,
    val questionType: String = "MULTIPLE_CHOICE",
    val questionText: String,
    val imageUrl: String? = null,
    val points: Double = 1.0,
    val sortOrder: Int = 0,
    val correctAnswer: String? = null,
    val options: List<ExamOptionDto> = emptyList()
)

data class ExamOptionDto(
    val id: String,
    val optionText: String,
    val optionLabel: String,
    val isCorrect: Boolean = false,
    val sortOrder: Int = 0
)

data class ExamAttemptDto(
    val id: String,
    val examId: String,
    val examTitle: String,
    val userId: String,
    val startedAt: String,
    val submittedAt: String? = null,
    val isSubmitted: Boolean = false,
    val autoScore: Double? = null,
    val finalScore: Double? = null,
    val durationSeconds: Int? = null,
    val passed: Boolean? = null
)

data class ExamAnswerDto(
    val id: String,
    val questionId: String,
    val questionText: String,
    val answerText: String? = null,
    val selectedOption: String? = null,
    val isCorrect: Boolean? = null,
    val score: Double? = null,
    val correctAnswer: String? = null
)

data class ExamResultDto(
    val attempt: ExamAttemptDto,
    val answers: List<ExamAnswerDto> = emptyList()
)

data class CreateExamRequest(
    val title: String,
    val description: String? = null,
    val courseId: String? = null,
    val channelId: String? = null,
    val startsAt: String,
    val endsAt: String,
    val durationMinutes: Int = 60,
    val totalScore: Double = 0.0,
    val passScore: Double? = null,
    val isPublic: Boolean = false,
    val shuffleQuestions: Boolean = false,
    val shuffleOptions: Boolean = false,
    val showResultsAfter: Boolean = true,
    val maxAttempts: Int = 1
)

data class AddQuestionRequest(
    val questionType: String = "MULTIPLE_CHOICE",
    val questionText: String,
    val imageUrl: String? = null,
    val points: Double = 1.0,
    val sortOrder: Int = 0,
    val correctAnswer: String? = null,
    val options: List<QuestionOptionRequest> = emptyList()
)

data class QuestionOptionRequest(
    val optionText: String,
    val optionLabel: String,
    val isCorrect: Boolean = false,
    val sortOrder: Int = 0
)

data class SubmitAnswerRequest(
    val questionId: String,
    val selectedOption: String? = null,
    val answerText: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Advertisement DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class CreateAdRequestDto(
    val messageContent: String,
    val targetChannelId: String,
    val sourceType: String = "CHAT",
    val sourceId: String? = null,
    val sourceMessageId: String = "",
    val messageMediaUrl: String? = null,
    val messageType: String = "TEXT"
)

data class AdRequestResponseDto(
    val id: String,
    val status: String
)

data class DeviceSessionDto(
    val id: String,
    val deviceName: String? = null,
    val platform: String? = null,
    val osVersion: String? = null,
    val appVersion: String? = null,
    val lastActiveAt: String? = null,
    val isCurrent: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💡 Feedback DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class CreateFeedbackRequestDto(
    val title: String,
    val description: String,
    val rating: Int
)

data class FeedbackResponseDto(
    val id: String,
    val userId: String?,
    val userDisplayName: String?,
    val title: String,
    val description: String,
    val rating: Int,
    val status: String,
    val createdAt: String,
    val adminNote: String?
)
