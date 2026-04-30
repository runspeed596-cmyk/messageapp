package com.hasani.messageapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════════════════════════════
// 📦 Generic Response Wrapper
// ═══════════════════════════════════════════════════════════════════════════════
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
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

data class VerifyOtpRequest(val phoneNumber: String, val code: String)

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

    // Profile Details
    val university: String? = null,
    val fieldOfStudy: String? = null,
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

    // Privacy Settings
    val profileVisibility: String? = null,
    val onlineVisibility: String? = null,
    val phoneVisibility: String? = null
)

// Positional args must match UserRepository usage
data class UpdateUserRequest(
    val username: String? = null,
    val displayName: String? = null,
    val bio: String? = null,
    val university: String? = null,
    val fieldOfStudy: String? = null,
    val education: String? = null,
    val skills: String? = null,
    val interests: String? = null,
    val workExperience: String? = null,
    val achievements: String? = null,
    val avatarUrl: String? = null,
    // Feature 3: Bio channels
    val bioChannelId1: String? = null,
    val bioChannelId2: String? = null
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
    val amplitudes: List<Int>? = null
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
    val isArchived: Boolean = false
)

data class GroupListResponse(
    val groups: List<GroupDto>
)

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val memberIds: List<String> = emptyList(),
    val avatarUrl: String? = null
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
    val isEdited: Boolean = false,
    val createdAt: String? = null,
    val editedAt: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val myReaction: String? = null,
    val poll: PollDto? = null,
    val amplitudes: List<Int>? = null
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
    val memberIds: List<String> = emptyList()
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
    val amplitudes: List<Int>? = null
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
    val achievements: String? = null
)

data class UpdateProfileDetailsRequest(
    val university: String? = null,
    val fieldOfStudy: String? = null,
    val education: String? = null,
    val skills: String? = null,
    val interests: String? = null,
    val workExperience: String? = null,
    val achievements: String? = null
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
    val type: String? = null
)

data class EntertainmentResponse(
    val movies: List<MovieDto>,
    val music: List<MusicDto>,
    val challenges: List<GameChallengeDto>
)
