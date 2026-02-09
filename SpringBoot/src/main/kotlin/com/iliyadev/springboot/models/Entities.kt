package com.iliyadev.springboot.models

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID
import com.fasterxml.jackson.annotation.JsonFormat

// ═══════════════════════════════════════════════════════════════════════════════
// 📦 Enums
// ═══════════════════════════════════════════════════════════════════════════════

enum class ChatType {
    PRIVATE,
    GROUP,
    CHANNEL
}

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    VOICE,
    AUDIO,  // Added to match Android client - for audio file attachments (not voice recordings)
    FILE,
    LOCATION,
    CONTACT,
    STICKER,
    POLL
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

enum class MemberRole {
    OWNER,
    ADMIN,
    MEMBER
}

enum class VisibilityOption {
    EVERYONE,
    CONTACTS,
    NOBODY
}

enum class FollowStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

enum class CollaborationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
}

enum class NotificationType {
    FOLLOW,
    FOLLOW_REQUEST,
    COLLABORATION_REQUEST,
    COLLABORATION_ACCEPTED,
    COLLABORATION_REJECTED,
    NEW_MESSAGE,
    SYSTEM
}

enum class EducationLevel {
    HIGH_SCHOOL,
    ASSOCIATE,
    BACHELOR,
    MASTER,
    DOCTORATE,
    OTHER
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 User Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(unique = true)
    var username: String = "",
    var displayName: String = "",
    @Column(unique = true)
    var phoneNumber: String = "",
    var avatarUrl: String? = null,
    @Column(length = 500)
    var bio: String? = null,
    var isOnline: Boolean = false,
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    var points: Long = 0, // Added for rewards
    var lastSeen: Instant? = null,
    var createdAt: Instant = Instant.now(),
    var passwordHash: String? = null,
    // Bio channels (max 2) - Feature 3
    var bioChannelId1: UUID? = null,
    var bioChannelId2: UUID? = null,
    // Premium status for story limit - Feature 4
    var isPremium: Boolean = false,
    // Privacy settings
    @Enumerated(EnumType.STRING)
    var profileVisibility: VisibilityOption = VisibilityOption.EVERYONE,
    @Enumerated(EnumType.STRING)
    var onlineVisibility: VisibilityOption = VisibilityOption.EVERYONE,
    @Enumerated(EnumType.STRING)
    var phoneVisibility: VisibilityOption = VisibilityOption.CONTACTS,
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var profileDetails: UserProfileDetails? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat Entity (Private Conversations)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "chats")
class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Enumerated(EnumType.STRING)
    var type: ChatType = ChatType.PRIVATE,
    var title: String = "",
    var avatarUrl: String? = null,
    var isPinned: Boolean = false,
    var isMuted: Boolean = false,
    var isArchived: Boolean = false,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "chat_participants",
        joinColumns = [JoinColumn(name = "chat_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var participants: MutableList<User> = mutableListOf()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Message Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "messages")
class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    var chat: Chat? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    var sender: User? = null,
    @Enumerated(EnumType.STRING)
    var type: MessageType = MessageType.TEXT,
    @Column(length = 10000)
    var content: String = "",
    var mediaUrl: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    var replyTo: Message? = null,
    var forwardedFrom: String? = null,
    @Enumerated(EnumType.STRING)
    var status: MessageStatus = MessageStatus.SENT,
    var isEdited: Boolean = false,
    var createdAt: Instant = Instant.now(),
    var editedAt: Instant? = null,
    @OneToMany(mappedBy = "message", cascade = [CascadeType.ALL], orphanRemoval = true)
    var reactions: MutableList<MessageReaction> = mutableListOf(),
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "poll_id")
    var poll: Poll? = null,
    @ElementCollection
    var amplitudes: MutableList<Int> = mutableListOf()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "groups")
class Group(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var name: String = "",
    @Column(length = 1000)
    var description: String? = null,
    var avatarUrl: String? = null,
    var isPublic: Boolean = false,
    var inviteLink: String? = null,
    var isInviteLinkEnabled: Boolean = true,
    var allowMembersToSendMessages: Boolean = true,
    var allowMembersToEditInfo: Boolean = false,
    var createdAt: Instant = Instant.now(),
    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], orphanRemoval = true)
    var members: MutableList<GroupMember> = mutableListOf(),
    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], orphanRemoval = true)
    var messages: MutableList<GroupMessage> = mutableListOf(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: User? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Member Entity (Junction Table)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "group_members")
class GroupMember(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: Group? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    @Enumerated(EnumType.STRING)
    var role: MemberRole = MemberRole.MEMBER,
    var joinedAt: Instant = Instant.now(),
    var isMuted: Boolean = false,
    var isPinned: Boolean = false,
    var isArchived: Boolean = false,
    // Admin permissions - Feature 6
    var canEditInfo: Boolean = false,
    var canPostStory: Boolean = false,
    var canAddMembers: Boolean = false,
    var canRemoveMembers: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Group Message Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "group_messages")
class GroupMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: Group? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    var sender: User? = null,
    @Enumerated(EnumType.STRING)
    var type: MessageType = MessageType.TEXT,
    @Column(length = 10000)
    var content: String = "",
    var mediaUrl: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    var replyTo: GroupMessage? = null,
    var isEdited: Boolean = false,
    var createdAt: Instant = Instant.now(),
    var editedAt: Instant? = null,
    @OneToMany(mappedBy = "message", cascade = [CascadeType.ALL], orphanRemoval = true)
    var reactions: MutableList<GroupMessageReaction> = mutableListOf(),
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "poll_id")
    var poll: Poll? = null,
    @ElementCollection
    var amplitudes: MutableList<Int> = mutableListOf()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "channels")
class Channel(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var name: String = "",
    @Column(length = 1000)
    var description: String? = null,
    var avatarUrl: String? = null,
    var isPublic: Boolean = true,
    @Column(unique = true)
    var publicId: String? = null, // Unique public identifier (e.g., "@mychannel")
    var inviteLink: String? = null,
    var createdAt: Instant = Instant.now(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    var owner: User? = null,
    @OneToMany(mappedBy = "channel", cascade = [CascadeType.ALL], orphanRemoval = true)
    var subscribers: MutableList<ChannelSubscriber> = mutableListOf(),
    @OneToMany(mappedBy = "channel", cascade = [CascadeType.ALL], orphanRemoval = true)
    var posts: MutableList<ChannelPost> = mutableListOf()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Subscriber Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "channel_subscribers")
class ChannelSubscriber(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    var channel: Channel? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    var isAdmin: Boolean = false,
    var subscribedAt: Instant = Instant.now(),
    var isMuted: Boolean = false,
    var isPinned: Boolean = false,
    var isArchived: Boolean = false,
    // Admin permissions - Feature 6
    var canEditInfo: Boolean = false,
    var canPostStory: Boolean = false,
    var canAddMembers: Boolean = false,
    var canRemoveMembers: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Post Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "channel_posts")
class ChannelPost(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    var channel: Channel? = null,
    @Enumerated(EnumType.STRING)
    var type: MessageType = MessageType.TEXT,
    @Column(length = 10000)
    var content: String = "",
    var mediaUrl: String? = null,
    var viewCount: Int = 0,
    var commentsEnabled: Boolean = true,
    var createdAt: Instant = Instant.now(),
    var editedAt: Instant? = null,
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "poll_id")
    var poll: Poll? = null,
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    var reactions: MutableList<ChannelPostReaction> = mutableListOf(),
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<ChannelPostComment> = mutableListOf(),
    @ElementCollection
    var amplitudes: MutableList<Int> = mutableListOf()
)

@Entity
@Table(name = "channel_post_reactions")
class ChannelPostReaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: ChannelPost? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    var reaction: String = "",
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "channel_post_comments")
class ChannelPostComment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: ChannelPost? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    @Column(length = 2000)
    var content: String = "",
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔐 OTP Entity for Phone Verification
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "otp_codes")
class OtpCode(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var phoneNumber: String = "",
    var code: String = "",
    var expiresAt: Instant = Instant.now(),
    var isUsed: Boolean = false,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔄 Refresh Token Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    var token: String = "",
    var expiresAt: Instant = Instant.now(),
    var isRevoked: Boolean = false,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 👍 Reaction Entities
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "message_reactions")
class MessageReaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    var message: Message? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    var reaction: String = "",
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "group_message_reactions")
class GroupMessageReaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    var message: GroupMessage? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    var reaction: String = "",
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📸 Story Entities
// ═══════════════════════════════════════════════════════════════════════════════

enum class StoryType {
    IMAGE,
    VIDEO
}

@Entity
@Table(name = "stories")
class Story(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    // Feature 7: Channel/Group stories
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    var channel: Channel? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: Group? = null,
    var mediaUrl: String = "",
    @Enumerated(EnumType.STRING)
    var type: StoryType = StoryType.IMAGE,
    @Column(length = 1000)
    var caption: String? = null,
    var durationSeconds: Int = 5,
    var createdAt: Instant = Instant.now(),
    var expiresAt: Instant = Instant.now().plusSeconds(24 * 3600), // Default 24h
    @OneToMany(mappedBy = "story", cascade = [CascadeType.ALL], orphanRemoval = true)
    var views: MutableList<StoryView> = mutableListOf()
)

@Entity
@Table(name = "story_views")
class StoryView(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    var story: Story? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    var viewedAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📊 Poll Entities
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "polls")
class Poll(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var question: String = "",
    var isMultipleChoice: Boolean = false,
    var isAnonymous: Boolean = false,
    var createdAt: Instant = Instant.now(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    var creator: User? = null,
    @OneToMany(mappedBy = "poll", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("id ASC")
    var options: MutableList<PollOption> = mutableListOf(),
    @OneToMany(mappedBy = "poll", cascade = [CascadeType.ALL], orphanRemoval = true)
    var votes: MutableList<PollVote> = mutableListOf()
)

@Entity
@Table(name = "poll_options")
class PollOption(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    var poll: Poll? = null,
    var text: String = "",
    var voteCount: Int = 0
)

@Entity
@Table(name = "poll_votes")
class PollVote(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    var poll: Poll? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    var option: PollOption? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    var votedAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 User Profile Details Entity (Professional Info)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "user_profile_details")
class UserProfileDetails(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    var user: User? = null,
    var university: String? = null,
    var fieldOfStudy: String? = null,
    var education: String? = null,
    @Column(length = 2000)
    var interests: String? = null,
    @Column(length = 2000)
    var achievements: String? = null,
    @Column(length = 2000)
    var skills: String? = null,
    @Column(length = 5000)
    var workExperience: String? = null,
    var updatedAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 User Follow Entity (Follower/Following System)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(
    name = "user_follows",
    uniqueConstraints = [UniqueConstraint(columnNames = ["follower_id", "following_id"])]
)
class UserFollow(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    var follower: User? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    var following: User? = null,
    @Enumerated(EnumType.STRING)
    var status: FollowStatus = FollowStatus.ACCEPTED,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🤝 Collaboration Request Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "collaboration_requests")
class CollaborationRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    var sender: User? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    var receiver: User? = null,
    var title: String = "",
    @Column(length = 2000)
    var message: String = "",
    @Enumerated(EnumType.STRING)
    var status: CollaborationStatus = CollaborationStatus.PENDING,
    var createdAt: Instant = Instant.now(),
    var respondedAt: Instant? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Notification Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "notifications")
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    @Enumerated(EnumType.STRING)
    var type: NotificationType = NotificationType.SYSTEM,
    var title: String = "",
    @Column(length = 1000)
    var body: String = "",
    var relatedEntityId: UUID? = null,
    var actorId: UUID? = null,
    var actorName: String? = null,
    var actorAvatarUrl: String? = null,
    var isRead: Boolean = false,
    var createdAt: Instant = Instant.now()
)


// ═══════════════════════════════════════════════════════════════════════════════
// 🖼️ Home Management Entities
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "home_banners")
class HomeBanner(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var title: String = "",
    var imageUrl: String = "",
    var linkUrl: String? = null,
    var colorStart: Long = 0xFF6200EA,
    var colorEnd: Long = 0xFFEC407A,
    var displayOrder: Int = 0,
    var isActive: Boolean = true,
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "universities")
class University(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(nullable = false)
    var name: String = "",
    // Location hierarchy
    var country: String? = null,
    var province: String? = null,
    var city: String? = null,
    var ministryName: String? = null, // Ministry of Science, Health, etc.
    var type: String? = null, // Public, Private, Azad, etc.
    var establishedYear: Int? = null,
    var studentCount: Int = 0,
    @Column(name = "iran_rank", nullable = false, columnDefinition = "INT DEFAULT 0")
    var iranRank: Int = 0,
    @Column(name = "world_rank", nullable = false, columnDefinition = "INT DEFAULT 0")
    var worldRank: Int = 0,
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    var articleCount: Int = 0,
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    var journalCount: Int = 0,
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    var paperCount: Int = 0,
    @Column(length = 2000)
    var facilities: String? = null,
    @Column(length = 2000)
    var faculties: String? = null, // JSON or comma-separated list of faculty names
    @Column(length = 5000)
    var departments: String? = null, // Detailed list of departments
    
    // Coordinates for the Globe/World of Science
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    
    var imageUrl: String? = null,
    var websiteUrl: String? = null,
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "discounts")
class Discount(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var title: String = "",
    var brandName: String = "",
    var percent: Int = 0,
    var code: String? = null,
    var description: String? = null,
    var imageUrl: String? = null,
    @field:JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    var expiryDate: java.time.LocalDate? = null,
    var category: String? = null,
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "entertainment_movies")
class EntertainmentMovie(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var title: String = "",
    @Column(length = 2000)
    var description: String? = null,
    var videoUrl: String = "",
    var thumbnailUrl: String? = null,
    var duration: String? = null,
    var releaseDate: String? = null,
    var isActive: Boolean = true,
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "entertainment_music")
class EntertainmentMusic(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var title: String = "",
    var artist: String? = null,
    var audioUrl: String = "",
    var coverUrl: String? = null,
    var duration: String? = null,
    var isActive: Boolean = true,
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "entertainment_riddles")
class EntertainmentRiddle(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var title: String = "",
    @Column(length = 2000)
    var description: String? = null,
    @Column(length = 2000)
    var question: String = "",
    var reward: String? = null,
    var type: String = "RIDDLE", // RIDDLE, QUIZ, CODE
    var isMultipleChoice: Boolean = false,
    @OneToMany(mappedBy = "riddle", cascade = [CascadeType.ALL], orphanRemoval = true)
    var options: MutableList<RiddleOption> = mutableListOf(),
    var correctAnswerIndex: Int? = null,
    var isActive: Boolean = true,
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "riddle_options")
class RiddleOption(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "riddle_id")
    var riddle: EntertainmentRiddle? = null,
    var text: String = "",
    var displayOrder: Int = 0
)
