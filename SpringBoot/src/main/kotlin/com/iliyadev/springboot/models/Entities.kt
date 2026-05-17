package com.iliyadev.springboot.models

import jakarta.persistence.*
import org.hibernate.annotations.BatchSize
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
    VIDEO_NOTE,  // Circular video message (like Telegram video notes)
    VOICE,
    AUDIO,  // Added to match Android client - for audio file attachments (not voice recordings)
    FILE,
    LOCATION,
    CONTACT,
    STICKER,
    GIF,
    POLL,
    LINK
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED,
    SCHEDULED
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
    SYSTEM,
    ADMIN_INVITE,
    TEACHER_INVITE
}

enum class OfficialGroupCategory {
    STUDENTS_IRAN,        // گروه رسمی دانشجویان ایران زمین
    MY_FIELD,             // گروه کشوری رشته من
    MY_UNIVERSITY,        // گروه رسمی دانشگاه من
    MY_FIELD_UNIVERSITY,  // گروه رسمی رشته دانشگاه من
    MY_UNION,             // گروه رسمی شورای صنفی من
    TEACHERS,             // گروه دبیران
    QA_SCIENCE,
    COURSE_GROUP
}

enum class OfficialDisplayMode {
    SPECIAL,   // Show in Special Folder
    TAB,       // Show in regular Groups/Channels tab only
    SUPPORT    // Show in Support contact section
}

enum class OfficialChannelCategory {
    STUDENTS_IRAN,       // کانال رسمی دانشجویان ایران زمین
    MY_FIELD,            // کانال کشوری رشته من
    MY_UNIVERSITY,       // کانال رسمی دانشگاه من
    MY_UNION,            // کانال رسمی شورای صنفی من
    FREELANCING,         // کانال رسمی فریلنسری دانشجویی
    PODCAST,             // کانال رسمی پادکست دانشجویی
    JOURNAL,             // کانال رسمی نشریه دانشجویی
    RESEARCH,            // کانال رسمی پروژه‌های تحقیقاتی
    COMPETITIONS,        // کانال مسابقات، جشنواره‌ها و کنگره‌ها
    SCIENCE_TECH,        // کانال رسمی علم + تکنولوژی
    EDUCATION,           // کانال رسمی آموزش
    STUDENT_NEWS,        // کانال اخبار دانشجویی کلاسور
    ENTERTAINMENT,       // کانال تفریح و سرگرمی
    APP_OFFICIAL,        // کانال رسمی اپلیکیشن کلاسور
    LOTTERY_DISCOUNT,     // کانال رسمی قرعه‌کشی و تخفیفات
    TEACHERS             // کانال اساتید
}

enum class VerificationStatus {
    NONE, PENDING_VERIFICATION, APPROVED, REJECTED
}

enum class SubscriptionTier {
    NONE, BASIC, PREMIUM, INSTITUTIONAL
}

enum class CourseStatus {
    DRAFT, PENDING, APPROVED, REJECTED, ACTIVE, COMPLETED, CANCELLED
}

enum class ExamStatus {
    DRAFT, SCHEDULED, ACTIVE, ENDED, GRADED
}

enum class QuestionType {
    MULTIPLE_CHOICE, FILL_BLANK, SHORT_ANSWER, DESCRIPTIVE, IMAGE_BASED
}

enum class ContentLockStatus {
    UNLOCKED, LOCKED, ARCHIVED
}

enum class ModerationStatus {
    PENDING, APPROVED, REJECTED
}

enum class TransactionType {
    DEPOSIT, WITHDRAWAL, PURCHASE, REFUND, SUBSCRIPTION, INTERNAL_TEST_PURCHASE
}

enum class ChannelClassification {
    GENERAL, VERIFIED_TEACHER, ELM_CLUB_INSTITUTION,
    COURSE_CHANNEL, HASHTAG_NATIONAL, HASHTAG_UNIVERSITY, HASHTAG_BRANCH
}

enum class FolderType {
    TEACHERS, ELM_CLUB, COURSES, PURCHASED
}

enum class UserRole {
    NORMAL, TEACHER, INSTITUTION, ADMIN, SUPER_ADMIN
}

enum class InstitutionType {
    CLUB, SCIENTIFIC_ASSOCIATION, INSTITUTE, STUDENT_ORG, RESEARCH_CENTER, INDEPENDENT, ASSOCIATION, ACADEMY, COMMUNITY
}

// Dynamic educational role system - values are managed via Admin Panel

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
    var firstName: String? = null,
    var lastName: String? = null,
    @Column(unique = true)
    var nationalCode: String? = null,
    @Column(length = 50)
    var educationalRole: String? = null,
    var gradeLevel: String? = null,
    var major: String? = null,
    var faculty: String? = null,
    var birthDate: java.time.LocalDate? = null,
    @Column(unique = true)
    var phoneNumber: String = "",
    var avatarUrl: String? = null,
    @Column(length = 500)
    var bio: String? = null,
    var isOnline: Boolean = false,
    @Column(nullable = false)
    var points: Long = 0, // Added for rewards
    var lastSeen: Instant? = null,
    var createdAt: Instant = Instant.now(),
    var passwordHash: String? = null,
    // Bio channels (max 2) - Feature 3
    var bioChannelId1: UUID? = null,
    var bioChannelId2: UUID? = null,
    // Premium status for story limit - Feature 4
    var isPremium: Boolean = false,
    // Mosbat Elm: Role system
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: UserRole = UserRole.NORMAL,
    // Privacy settings
    @Enumerated(EnumType.STRING)
    var profileVisibility: VisibilityOption? = VisibilityOption.EVERYONE,
    @Enumerated(EnumType.STRING)
    var onlineVisibility: VisibilityOption? = VisibilityOption.EVERYONE,
    @Enumerated(EnumType.STRING)
    var phoneVisibility: VisibilityOption? = VisibilityOption.CONTACTS,
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var profileDetails: UserProfileDetails? = null,
    // Favorite courses
    @ElementCollection
    @CollectionTable(name = "user_favorite_courses", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "course_id")
    var favoriteCourseIds: MutableSet<UUID> = mutableSetOf(),
    // Link to Institution (Academy)
    var institutionId: UUID? = null,
    var institutionLogoUrl: String? = null,
    var institutionName: String? = null,
    var averageRating: Double = 0.0,
    var reviewCount: Int = 0,
    var officialChannelId: UUID? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📱 User Session Entity (Active Devices)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "user_sessions")
class UserSession(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var userId: UUID = UUID.randomUUID(),

    var deviceName: String? = null,
    var platform: String? = null,
    var osVersion: String? = null,
    var appVersion: String? = null,
    var lastActiveIp: String? = null,
    var lastActiveAt: Instant = Instant.now(),
    var createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var isActive: Boolean = true
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
    @BatchSize(size = 50)
    var reactions: MutableList<MessageReaction> = mutableListOf(),
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "poll_id")
    var poll: Poll? = null,
    @ElementCollection
    @BatchSize(size = 50)
    var amplitudes: MutableList<Int> = mutableListOf(),
    // Pin support
    @Column(nullable = false)
    var isPinned: Boolean = false,
    var pinnedAt: Instant? = null,
    var pinnedById: UUID? = null,
    // Scheduled messages
    var scheduledAt: Instant? = null,
    // Interactive features
    var actionLabel: String? = null,
    var actionUrl: String? = null,
    var timerTargetAt: Instant? = null
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
    var createdBy: User? = null,
    // Special Folder (Official) fields
    @Column(nullable = false)
    var isOfficial: Boolean = false,
    @Column(nullable = false)
    var isSystemOfficial: Boolean = false,
    @Enumerated(EnumType.STRING)
    var officialCategory: OfficialGroupCategory? = null,
    @Column(nullable = false)
    var hideMembers: Boolean = false,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var displayMode: OfficialDisplayMode = OfficialDisplayMode.SPECIAL,
    // Audience targeting fields (all null = عمومی/public)
    var targetFieldOfStudy: String? = null,
    var targetEducationLevel: String? = null,
    var targetProvince: String? = null,
    var targetCity: String? = null,
    var targetUniversity: String? = null,
    var targetMinistry: String? = null,
    var targetAudienceType: String? = null
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
    var canRemoveMembers: Boolean = false,
    // Mandatory channel support
    var isMandatory: Boolean = false
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
    var forwardedFrom: String? = null,
    var isEdited: Boolean = false,
    var createdAt: Instant = Instant.now(),
    var editedAt: Instant? = null,
    @OneToMany(mappedBy = "message", cascade = [CascadeType.ALL], orphanRemoval = true)
    @BatchSize(size = 50)
    var reactions: MutableList<GroupMessageReaction> = mutableListOf(),
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "poll_id")
    var poll: Poll? = null,
    @ElementCollection
    @BatchSize(size = 50)
    var amplitudes: MutableList<Int> = mutableListOf(),
    // Pin support
    @Column(nullable = false)
    var isPinned: Boolean = false,
    var pinnedAt: Instant? = null,
    var pinnedById: UUID? = null,
    // Scheduled messages
    var scheduledAt: Instant? = null,
    // Interactive features
    var actionLabel: String? = null,
    var actionUrl: String? = null,
    var timerTargetAt: Instant? = null
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
    var posts: MutableList<ChannelPost> = mutableListOf(),
    // Special Folder (Official) fields
    @Column(nullable = false)
    var isOfficial: Boolean = false,
    @Column(nullable = false)
    var isSystemOfficial: Boolean = false,
    @Enumerated(EnumType.STRING)
    var officialCategory: OfficialChannelCategory? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var displayMode: OfficialDisplayMode = OfficialDisplayMode.SPECIAL,
    // Audience targeting fields (all null = عمومی/public)
    var targetFieldOfStudy: String? = null,
    var targetEducationLevel: String? = null,
    var targetProvince: String? = null,
    var targetCity: String? = null,
    var targetUniversity: String? = null,
    var targetMinistry: String? = null,
    var targetAudienceType: String? = null,
    // Mosbat Elm: Channel classification & DRM
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var classification: ChannelClassification = ChannelClassification.GENERAL,
    @Column(nullable = false)
    var isVerifiedTeacher: Boolean = false,
    var institutionId: UUID? = null
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
    var canRemoveMembers: Boolean = false,
    // Mandatory channel support
    @Column(nullable = false, columnDefinition = "boolean default false")
    var isMandatory: Boolean = false
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
    var forwardedFrom: String? = null,
    var createdAt: Instant = Instant.now(),
    var editedAt: Instant? = null,
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "poll_id")
    var poll: Poll? = null,
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    @BatchSize(size = 50)
    var reactions: MutableList<ChannelPostReaction> = mutableListOf(),
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<ChannelPostComment> = mutableListOf(),
    @ElementCollection
    @BatchSize(size = 50)
    var amplitudes: MutableList<Int> = mutableListOf(),
    // Pin support
    @Column(nullable = false)
    var isPinned: Boolean = false,
    var pinnedAt: Instant? = null,
    var pinnedById: UUID? = null,
    // Scheduled messages
    var scheduledAt: Instant? = null,
    // Interactive features
    var actionLabel: String? = null,
    var actionUrl: String? = null,
    var timerTargetAt: Instant? = null,
    // Advertisement support
    @Column(nullable = false)
    var isAd: Boolean = false,
    var adLabel: String? = null,
    var adSourceChannelId: UUID? = null
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
    var sessionId: UUID? = null, // Added to link refresh token with a specific session
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
    var university: String? = null, // Deprecated, kept for backward compatibility
    var fieldOfStudy: String? = null, // Deprecated
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_profile_universities", joinColumns = [JoinColumn(name = "profile_id")])
    var universities: MutableSet<String> = mutableSetOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_profile_fields", joinColumns = [JoinColumn(name = "profile_id")])
    var fieldsOfStudy: MutableSet<String> = mutableSetOf(),

    var isGraduated: Boolean = false,
    
    var education: String? = null,
    var faculty: String? = null,
    @Column(length = 2000)
    var interests: String? = null,
    @Column(length = 2000)
    var achievements: String? = null,
    @Column(length = 2000)
    var skills: String? = null,
    @Column(length = 5000)
    var workExperience: String? = null,
    // Teacher role
    var isTeacher: Boolean = false,
    var teachingField: String? = null,
    var teachingUniversity: String? = null,
    // Location for targeting
    var province: String? = null,
    var city: String? = null,
    
    // Academy Profile (Mosbat Elm Organizer)
    var academyName: String? = null,
    @Column(length = 1000)
    var academyHashtags: String? = null,
    
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
    var createdAt: Instant = Instant.now(),
    // Mosbat Elm: Subscription notification tier
    @Column(nullable = false)
    var isSubscriptionNotification: Boolean = false,
    @Column(nullable = false, length = 10)
    var notificationTier: String = "NORMAL",  // NORMAL (red) or GOLDEN (subscription)
    var status: String = "PENDING"
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
    var isActive: Boolean = true,
    var section: String = "HOME", // "HOME", "MOSBAT_ELM"
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "clubs")
class Club(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: java.util.UUID? = null,
    var name: String = "",
)

@Entity
@Table(name = "student_orgs")
class StudentOrg(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: java.util.UUID? = null,
    var name: String = "",
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
    @Column(columnDefinition = "TEXT")
    var rankings: String? = null,
    @Column(nullable = false)
    var articleCount: Int = 0,
    @Column(nullable = false)
    var journalCount: Int = 0,
    @Column(nullable = false)
    var paperCount: Int = 0,
    var createdAt: Instant = Instant.now(),
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
    
    // Extended fields
    @Column(length = 5000)
    var honors: String? = null, // افتخارات
    @Column(nullable = false)
    var professorCount: Int = 0, // تعداد اساتید
    @Column(length = 5000)
    var professorNames: String? = null, // نام اساتید هر رشته
    @Column(nullable = false)
    var publicationCount: Int = 0, // تعداد مجلات و نشریات
    @Column(length = 5000)
    var studentOrgs: String? = null, // انجمن‌ها، کانون‌ها و مجموعه‌های دانشجویی
    @Column(length = 5000)
    var lastAdmissionCapacity: String? = null // آخرین ظرفیت پذیرش هر رشته
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
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Story Reply Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "story_replies")
class StoryReply(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    var story: Story? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    @Column(length = 2000)
    var content: String = "",
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🤖 AI Bot Entity (Special Folder)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "ai_bots")
class AiBot(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var name: String = "",
    var botType: String = "",
    var category: String = "GENERAL", // GENERAL or SPECIALIST
    @Column(length = 500)
    var description: String? = null,
    var avatarUrl: String? = null,
    var isActive: Boolean = true,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🤖 AI Bot Message Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "ai_bot_messages")
class AiBotMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(nullable = false)
    var botId: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    var userId: UUID = UUID.randomUUID(),
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String = "",
    @Column(nullable = false)
    var role: String = "USER", // USER or ASSISTANT
    var actionLabel: String? = null,
    var actionUrl: String? = null,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📚 Field of Study Entity (Admin-managed)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "fields_of_study")
class FieldOfStudy(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(nullable = false)
    var name: String = "",
    @Column(nullable = false)
    var educationLevel: String = "", // e.g. کارشناسی, کارشناسی ارشد, دکتری
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🎓 Education Level Entity (Admin-managed)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "education_levels")
class EducationLevel(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(nullable = false)
    var name: String = "", // e.g. کاردانی, کارشناسی, کارشناسی ارشد, دکتری
    var roleValueEn: String? = null, // Linked to EducationalRoleOption.valueEn
    @Column(nullable = false, columnDefinition = "boolean default false")
    var hasFieldOfStudy: Boolean = false,
    @Column(nullable = false, columnDefinition = "boolean default false")
    var hasFaculty: Boolean = false,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🎭 Educational Role Option Entity (Admin-managed)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "educational_role_options")
class EducationalRoleOption(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(nullable = false)
    var labelFa: String = "", // e.g. دانش‌آموز, دانشجو, استاد/معلم, آزاد
    @Column(nullable = false)
    var valueEn: String = "", // e.g. SCHOOL_STUDENT, UNI_STUDENT, TEACHER, FREELANCER
    var emoji: String = "📚", // e.g. 🎒, 🎓, 📚, 💼
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🏛️ Faculty Entity (Admin-managed reference data for World of Science)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "faculties")
class Faculty(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(nullable = false, unique = true)
    var name: String = "",
    var educationLevel: String? = null, // Linked to EducationLevel.name
    @Column(name = "display_order", nullable = false, columnDefinition = "integer default 0")
    var displayOrder: Int = 0,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Teacher Verification Request Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "teacher_verification_requests")
class TeacherVerificationRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    var fullName: String = "",
    @Column(length = 10)
    var nationalCode: String = "",
    var teachingField: String = "",
    var institution: String? = null,
    @ElementCollection
    @CollectionTable(name = "teacher_verif_documents", joinColumns = [JoinColumn(name = "request_id")])
    @Column(name = "document_url")
    var documentUrls: MutableList<String> = mutableListOf(),
    @Enumerated(EnumType.STRING)
    var status: VerificationStatus = VerificationStatus.PENDING_VERIFICATION,
    @Column(length = 1000)
    var adminNote: String? = null,
    var reviewedBy: UUID? = null,
    var reviewedAt: Instant? = null,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🏛️ Institution Entity (Elm Club — Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "institutions")
class Institution(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var name: String = "",
    @Enumerated(EnumType.STRING)
    var type: InstitutionType = InstitutionType.INDEPENDENT,
    var registrationNumber: String? = null,
    var contactPhone: String? = null,
    var contactEmail: String? = null,
    var province: String? = null,
    var city: String? = null,
    @Column(length = 1000)
    var address: String? = null,
    var logoUrl: String? = null,
    @Column(length = 2000)
    var description: String? = null,
    
    @Column(columnDefinition = "boolean default false")
    var isSubsidiary: Boolean = false,
    @Column(length = 2000)
    var dependencyDescription: String? = null,
    
    @ElementCollection
    @CollectionTable(name = "institution_universities", joinColumns = [JoinColumn(name = "institution_id")])
    @Column(name = "university_name")
    var universities: MutableList<String> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "institution_faculties", joinColumns = [JoinColumn(name = "institution_id")])
    @Column(name = "faculty_name")
    var faculties: MutableList<String> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "institution_specialties", joinColumns = [JoinColumn(name = "institution_id")])
    @Column(name = "specialty")
    var specialties: MutableList<String> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "institution_clubs", joinColumns = [JoinColumn(name = "institution_id")])
    @Column(name = "club_id")
    var associatedClubIds: MutableList<String> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "institution_fields", joinColumns = [JoinColumn(name = "institution_id")])
    @Column(name = "field_id")
    var associatedFieldOfStudyIds: MutableList<String> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "institution_student_orgs", joinColumns = [JoinColumn(name = "institution_id")])
    @Column(name = "org_id")
    var associatedStudentOrgIds: MutableList<String> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "institution_instructors", joinColumns = [JoinColumn(name = "institution_id")])
    @Column(name = "instructor_id")
    var instructorIds: MutableList<UUID> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "institution_admins", joinColumns = [JoinColumn(name = "institution_id")])
    @Column(name = "admin_id")
    var adminIds: MutableList<UUID> = mutableListOf(),

    @OneToMany(mappedBy = "institution", cascade = [CascadeType.ALL], orphanRemoval = true)
    var honors: MutableList<InstitutionHonor> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "institution_manual_instructors", joinColumns = [JoinColumn(name = "institution_id")])
    var manualInstructors: MutableList<ManualInstructor> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    var owner: User? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    var channel: Channel? = null,
    @Enumerated(EnumType.STRING)
    var verificationStatus: VerificationStatus = VerificationStatus.PENDING_VERIFICATION,
    @Column(length = 1000)
    var adminNote: String? = null,
    var reviewedBy: UUID? = null,
    var reviewedAt: Instant? = null,
    var isActive: Boolean = true,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
    var averageRating: Double = 0.0,
    var reviewCount: Int = 0
)

@Entity
@Table(name = "institution_honors")
class InstitutionHonor(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    var institution: Institution? = null,
    var title: String = "",
    @Column(length = 1000)
    var description: String? = null,
    var imageUrl: String? = null,
    var date: java.time.LocalDate? = null,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💳 Subscription Plan Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "subscription_plans")
class SubscriptionPlan(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var name: String = "",
    @Enumerated(EnumType.STRING)
    var tier: SubscriptionTier = SubscriptionTier.BASIC,
    var priceRials: Long = 0,
    var durationDays: Int = 30,
    var maxPromotions: Int = 1,
    @Column(columnDefinition = "TEXT")
    var features: String = "{}",  // JSON
    var isActive: Boolean = true,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💳 User Subscription Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "user_subscriptions")
class UserSubscription(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    var plan: SubscriptionPlan? = null,
    var startsAt: Instant = Instant.now(),
    var expiresAt: Instant = Instant.now(),
    var isActive: Boolean = true,
    var autoRenew: Boolean = false,
    var transactionId: UUID? = null,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💰 Wallet Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "wallets")
class Wallet(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    var user: User? = null,
    var balance: Long = 0,  // in Rials
    var isActive: Boolean = true,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💰 Wallet Transaction Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "wallet_transactions")
class WalletTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    var wallet: Wallet? = null,
    @Enumerated(EnumType.STRING)
    var type: TransactionType = TransactionType.PURCHASE,
    var amount: Long = 0,
    var balanceAfter: Long = 0,
    @Column(length = 500)
    var description: String? = null,
    var referenceId: UUID? = null,
    var referenceType: String? = null,  // CONTENT, COURSE, SUBSCRIPTION, EXAM
    var gatewayRef: String? = null,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📚 Course Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Embeddable
class CourseChapter(
    var title: String = "",
    var durationText: String = "",
    var sessionStartTime: Instant? = null,
    var sessionEndTime: Instant? = null
)

@Embeddable
class ManualInstructor(
    var name: String = "",
    var avatarUrl: String? = null,
    var resume: String? = null
)
@Entity
@Table(name = "courses")
class Course(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(length = 500)
    var title: String = "",
    @Column(length = 300)
    var slogan: String? = null,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    var favoritesCount: Int = 0,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_teachers",
        joinColumns = [JoinColumn(name = "course_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var teachers: MutableList<User> = mutableListOf(),
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_admins",
        joinColumns = [JoinColumn(name = "course_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var admins: MutableList<User> = mutableListOf(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    var organizer: User? = null,
    var institutionId: UUID? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    var channel: Channel? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: Group? = null,
    var coverImageUrl: String? = null,
    var fieldOfStudy: String? = null,
    var educationLevel: String? = null,
    var startsAt: Instant = Instant.now(),
    var endsAt: Instant = Instant.now(),
    var enrollmentLimit: Int? = null,
    var isPublic: Boolean = true,
    @Enumerated(EnumType.STRING)
    var status: CourseStatus = CourseStatus.DRAFT,
    @Column(columnDefinition = "TEXT")
    var adminNote: String? = null,
    var priceRials: Long = 0,
    var discountPercentage: Int = 0,
    var syllabusDuration: String? = null,
    var capacity: Int? = null,
    var averageRating: Double = 0.0,
    var reviewCount: Int = 0,
    @ElementCollection
    @CollectionTable(name = "course_collaborators", joinColumns = [JoinColumn(name = "course_id")])
    @Column(name = "collaborator_id")
    var collaborators: MutableList<String> = mutableListOf(),
    @ElementCollection
    @CollectionTable(name = "course_tags", joinColumns = [JoinColumn(name = "course_id")])
    @Column(name = "tag")
    var tags: MutableList<String> = mutableListOf(),
    @ElementCollection
    @CollectionTable(name = "course_manual_instructors", joinColumns = [JoinColumn(name = "course_id")])
    var manualInstructors: MutableList<ManualInstructor> = mutableListOf(),
    @ElementCollection
    @CollectionTable(name = "course_chapters", joinColumns = [JoinColumn(name = "course_id")])
    var chapters: MutableList<CourseChapter> = mutableListOf(),
    @ElementCollection
    @CollectionTable(name = "course_suitable_for", joinColumns = [JoinColumn(name = "course_id")])
    @Column(name = "audience")
    var suitableFor: MutableList<String> = mutableListOf(),
    @Column(columnDefinition = "TEXT")
    var organizerDescription: String? = null,
    var scientificAssociationName: String? = null,
    var isVerticalPoster: Boolean = false,
    @Column(nullable = false, columnDefinition = "bigint default 0")
    var viewCount: Long = 0,
    @Column(nullable = false, columnDefinition = "bigint default 0")
    var clickCount: Long = 0,
    // BBB Integration
    var bbbMeetingId: String? = null,
    var bbbAttendeePassword: String? = null,
    var bbbModeratorPassword: String? = null,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📚 Course Enrollment Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(
    name = "course_enrollments",
    uniqueConstraints = [UniqueConstraint(columnNames = ["course_id", "user_id"])]
)
class CourseEnrollment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    var course: Course? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    var enrolledAt: Instant = Instant.now(),
    var isActive: Boolean = true
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📚 Course Material Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "course_materials")
class CourseMaterial(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    var course: Course? = null,
    var title: String = "",
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    var contentUrl: String? = null,
    var contentType: String? = null,  // VIDEO, PDF, AUDIO, TEXT
    var sortOrder: Int = 0,
    var isLocked: Boolean = false,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Course Comment Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "course_comments")
class CourseComment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    var course: Course? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    @Column(columnDefinition = "TEXT")
    var content: String = "",
    var rating: Int = 0, // 1-5 stars
    var replyToCommentId: UUID? = null,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Exam Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "exams")
class Exam(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(length = 500)
    var title: String = "",
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    var creator: User? = null,
    var courseId: UUID? = null,
    var channelId: UUID? = null,
    var startsAt: Instant = Instant.now(),
    var endsAt: Instant = Instant.now(),
    var durationMinutes: Int = 60,
    @Column(precision = 10, scale = 2)
    var totalScore: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    @Column(precision = 10, scale = 2)
    var passScore: java.math.BigDecimal? = null,
    @Enumerated(EnumType.STRING)
    var status: ExamStatus = ExamStatus.DRAFT,
    var isPublic: Boolean = false,
    var shuffleQuestions: Boolean = false,
    var shuffleOptions: Boolean = false,
    var showResultsAfter: Boolean = true,
    var maxAttempts: Int = 1,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🤝 Course Collaboration Request Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "course_collaboration_requests")
class CourseCollaborationRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    var course: Course? = null,
    var senderInstitutionId: UUID? = null,
    var targetInstitutionId: UUID? = null,
    @Enumerated(EnumType.STRING)
    var status: CollaborationStatus = CollaborationStatus.PENDING,
    var message: String? = null,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Exam Access Rule Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "exam_access_rules")
class ExamAccessRule(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    var exam: Exam? = null,
    var ruleType: String = "PUBLIC",  // PUBLIC, CHANNEL_MEMBERS, SELECTED_USERS
    var channelId: UUID? = null,
    var userId: UUID? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Exam Question Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "exam_questions")
class ExamQuestion(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    var exam: Exam? = null,
    @Enumerated(EnumType.STRING)
    var questionType: QuestionType = QuestionType.MULTIPLE_CHOICE,
    @Column(columnDefinition = "TEXT")
    var questionText: String = "",
    var imageUrl: String? = null,
    @Column(precision = 10, scale = 2)
    var points: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    var sortOrder: Int = 0,
    @Column(columnDefinition = "TEXT")
    var correctAnswer: String? = null,
    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    var options: MutableList<ExamQuestionOption> = mutableListOf(),
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Exam Question Option Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "exam_question_options")
class ExamQuestionOption(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: ExamQuestion? = null,
    @Column(columnDefinition = "TEXT")
    var optionText: String = "",
    @Column(length = 5)
    var optionLabel: String = "",  // A, B, C, D
    var isCorrect: Boolean = false,
    var sortOrder: Int = 0
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Exam Attempt Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(
    name = "exam_attempts",
    uniqueConstraints = [UniqueConstraint(columnNames = ["exam_id", "user_id"])]
)
class ExamAttempt(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    var exam: Exam? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    var startedAt: Instant = Instant.now(),
    var submittedAt: Instant? = null,
    var isSubmitted: Boolean = false,
    @Column(precision = 10, scale = 2)
    var autoScore: java.math.BigDecimal? = null,
    @Column(precision = 10, scale = 2)
    var manualScore: java.math.BigDecimal? = null,
    @Column(precision = 10, scale = 2)
    var finalScore: java.math.BigDecimal? = null,
    var durationSeconds: Int? = null,
    @Column(length = 45)
    var ipAddress: String? = null,
    @Column(length = 500)
    var deviceInfo: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Exam Answer Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(
    name = "exam_answers",
    uniqueConstraints = [UniqueConstraint(columnNames = ["attempt_id", "question_id"])]
)
class ExamAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id")
    var attempt: ExamAttempt? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: ExamQuestion? = null,
    @Column(columnDefinition = "TEXT")
    var answerText: String? = null,
    @Column(length = 5)
    var selectedOption: String? = null,
    var isCorrect: Boolean? = null,
    @Column(precision = 10, scale = 2)
    var score: java.math.BigDecimal? = null,
    var gradedBy: UUID? = null,
    var gradedAt: Instant? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔒 Locked Content Entity (DRM — Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "locked_contents")
class LockedContent(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    var channel: Channel? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    var uploader: User? = null,
    @Column(length = 500)
    var title: String = "",
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    var contentType: String = "",  // VIDEO, AUDIO, FILE, TEXT, IMAGE
    @Column(length = 500)
    var storageKey: String = "",
    @Column(length = 500)
    var encryptionKey: String = "",
    var thumbnailUrl: String? = null,
    var priceRials: Long = 0,
    @Enumerated(EnumType.STRING)
    var lockStatus: ContentLockStatus = ContentLockStatus.LOCKED,
    var viewCount: Int = 0,
    var purchaseCount: Int = 0,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔒 Content Purchase Entity (DRM — Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(
    name = "content_purchases",
    uniqueConstraints = [UniqueConstraint(columnNames = ["content_id", "user_id"])]
)
class ContentPurchase(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    var content: LockedContent? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    var transaction: WalletTransaction? = null,
    var purchasedAt: Instant = Instant.now(),
    var expiresAt: Instant? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// #️⃣ Official Hashtag Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "official_hashtags")
class OfficialHashtag(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(unique = true)
    var tag: String = "",
    var displayNameFa: String = "",
    var category: String? = null,
    var nationalChannelId: UUID? = null,
    var universityChannelId: UUID? = null,
    var branchChannelId: UUID? = null,
    var isActive: Boolean = true,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// #️⃣ Hashtag Promotion Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "hashtag_promotions")
class HashtagPromotion(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    var hashtag: OfficialHashtag? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    @Column(columnDefinition = "TEXT")
    var contentText: String = "",
    @ElementCollection
    @CollectionTable(name = "promotion_media_urls", joinColumns = [JoinColumn(name = "promotion_id")])
    @Column(name = "media_url")
    var mediaUrls: MutableList<String> = mutableListOf(),
    @Enumerated(EnumType.STRING)
    var moderationStatus: ModerationStatus = ModerationStatus.PENDING,
    var moderatedBy: UUID? = null,
    var moderatedAt: Instant? = null,
    @Column(length = 500)
    var rejectionReason: String? = null,
    var publishedAt: Instant? = null,
    var subscriptionId: UUID? = null,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📁 Smart Folder Rule Entity (Mosbat Elm)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "smart_folder_rules")
class SmartFolderRule(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Enumerated(EnumType.STRING)
    var folderType: FolderType = FolderType.TEACHERS,
    @Enumerated(EnumType.STRING)
    var classification: ChannelClassification = ChannelClassification.VERIFIED_TEACHER,
    var iconName: String? = null,
    var labelFa: String = "",
    var isActive: Boolean = true
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Advertisement Request
// ═══════════════════════════════════════════════════════════════════════════════

enum class AdRequestStatus {
    PENDING, APPROVED, REJECTED
}

@Entity
@Table(name = "ad_requests")
class AdRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    var requester: User? = null,
    var sourceMessageId: String = "",
    var sourceType: String = "", // CHAT, GROUP, CHANNEL
    var sourceId: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_channel_id")
    var targetChannel: Channel? = null,
    @Column(length = 10000)
    var messageContent: String = "",
    var messageMediaUrl: String? = null,
    @Enumerated(EnumType.STRING)
    var messageType: MessageType = MessageType.TEXT,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: AdRequestStatus = AdRequestStatus.PENDING,
    var createdAt: Instant = Instant.now(),
    var reviewedAt: Instant? = null,
    var reviewedBy: UUID? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// ⚙️ Role to Channel Mapping Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "role_channel_mappings")
class RoleChannelMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(nullable = false, length = 50)
    var educationalRole: String = "",
    var gradeLevel: String? = null,
    var major: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    var channel: Channel? = null,
    var createdAt: Instant = Instant.now()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🛡️ Admin Panel User Entity (for admin panel authentication & management)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity
@Table(name = "panel_admins")
class PanelAdmin(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(unique = true, nullable = false)
    var username: String = "",
    @Column(nullable = false)
    var passwordHash: String = "",
    var displayName: String = "",
    @Column(nullable = false)
    var isSuperAdmin: Boolean = false,
    var createdAt: Instant = Instant.now(),
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "panel_admin_permissions", joinColumns = [JoinColumn(name = "admin_id")])
    @Column(name = "permission")
    var permissions: MutableList<String> = mutableListOf()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💡 Feedback and Suggestions
// ═══════════════════════════════════════════════════════════════════════════════

enum class FeedbackStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED
}

@Entity
@Table(name = "feedbacks")
class Feedback(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
    
    @Column(nullable = false, length = 255)
    var title: String = "",
    
    @Column(nullable = false, length = 2000)
    var description: String = "",
    
    var rating: Int = 5,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: FeedbackStatus = FeedbackStatus.OPEN,
    
    var createdAt: Instant = Instant.now(),
    
    @Column(length = 2000)
    var adminNote: String? = null
)


