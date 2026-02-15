package com.iliyadev.springboot.repositories

import com.iliyadev.springboot.models.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 User Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByPhoneNumber(phoneNumber: String): User?
    fun findByUsername(username: String): User?
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    fun existsByUsername(username: String): Boolean
    @Query("SELECT u FROM User u WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchByNameOrUsername(@Param("query") query: String, pageable: Pageable): Page<User>
    fun findByPhoneNumberIn(phoneNumbers: List<String>): List<User>
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface ChatRepository : JpaRepository<Chat, UUID> {
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.id = :userId ORDER BY c.updatedAt DESC")
    fun findByParticipantId(@Param("userId") userId: UUID, pageable: Pageable): Page<Chat>
    @Query("SELECT DISTINCT c FROM Chat c JOIN c.participants p1 JOIN c.participants p2 WHERE c.type = com.iliyadev.springboot.models.ChatType.PRIVATE AND p1.id = :userId1 AND p2.id = :userId2")
    fun findPrivateChatBetween(@Param("userId1") userId1: UUID, @Param("userId2") userId2: UUID): List<Chat>
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.id = :userId AND c.isArchived = false ORDER BY c.isPinned DESC, c.updatedAt DESC")
    fun findActiveChatsForUser(@Param("userId") userId: UUID, pageable: Pageable): Page<Chat>
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.id = :userId AND c.isArchived = true ORDER BY c.updatedAt DESC")
    fun findArchivedChatsForUser(@Param("userId") userId: UUID, pageable: Pageable): Page<Chat>
    
    fun findByParticipantsContainingAndType(participant: User, type: ChatType, pageable: Pageable): List<Chat>
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Message Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface MessageRepository : JpaRepository<Message, UUID> {
    fun findByChatIdOrderByCreatedAtDesc(chatId: UUID, pageable: Pageable): Page<Message>
    @EntityGraph(attributePaths = ["reactions", "sender", "replyTo"])
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdAt DESC")
    fun findWithReactionsByChatId(@Param("chatId") chatId: UUID, pageable: Pageable): Page<Message>
    fun findByChatIdAndCreatedAtBeforeOrderByCreatedAtDesc(chatId: UUID, before: Instant, pageable: Pageable): Page<Message>
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.status != 'READ'")
    fun countUnreadMessages(@Param("chatId") chatId: UUID, @Param("userId") userId: UUID): Long
    fun findTopByChatIdOrderByCreatedAtDesc(chatId: UUID): Message?
    fun findByChatIdAndIsPinnedTrue(chatId: UUID): List<Message>
    fun findByScheduledAtBeforeAndScheduledAtIsNotNull(time: Instant): List<Message>
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND (:query IS NULL OR LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.mediaUrl) LIKE LOWER(CONCAT('%', :query, '%'))) AND m.type IN :types")
    fun searchMessages(@Param("chatId") chatId: UUID, @Param("query") query: String?, @Param("types") types: List<MessageType>, pageable: Pageable): Page<Message>
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND (m.type IN :types OR (:includeLinks = true AND m.type = 'TEXT' AND (LOWER(m.content) LIKE '%http%' OR LOWER(m.content) LIKE '%www.%' OR LOWER(m.content) LIKE '%.com%' OR LOWER(m.content) LIKE '%.ir%' OR LOWER(m.content) LIKE '%.net%' OR LOWER(m.content) LIKE '%.org%' OR LOWER(m.content) LIKE '%.info%' OR LOWER(m.content) LIKE '%.io%' OR LOWER(m.content) LIKE '%.co%' OR LOWER(m.content) LIKE '%.me%'))) ORDER BY m.createdAt DESC")
    fun findSharedContent(@Param("chatId") chatId: UUID, @Param("types") types: List<MessageType>, @Param("includeLinks") includeLinks: Boolean, pageable: Pageable): Page<Message>
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface GroupRepository : JpaRepository<Group, UUID> {
    @Query("SELECT g FROM Group g JOIN GroupMember gm ON gm.group.id = g.id WHERE gm.user.id = :userId")
    fun findByMemberId(@Param("userId") userId: UUID, pageable: Pageable): Page<Group>
    fun findByIsPublicTrue(pageable: Pageable): Page<Group>
    @Query("SELECT g FROM Group g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchByName(@Param("query") query: String, pageable: Pageable): Page<Group>
    fun findByInviteLink(inviteLink: String): Group?
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Member Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface GroupMemberRepository : JpaRepository<GroupMember, UUID> {
    fun findByGroupIdAndUserId(groupId: UUID, userId: UUID): GroupMember?
    fun findByGroupId(groupId: UUID): List<GroupMember>
    fun existsByGroupIdAndUserId(groupId: UUID, userId: UUID): Boolean
    fun countByGroupId(groupId: UUID): Long
    fun deleteByGroupIdAndUserId(groupId: UUID, userId: UUID)
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.role IN ('OWNER', 'ADMIN')")
    fun findAdminsByGroupId(@Param("groupId") groupId: UUID): List<GroupMember>
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Group Message Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface GroupMessageRepository : JpaRepository<GroupMessage, UUID> {
    fun findByGroupIdOrderByCreatedAtDesc(groupId: UUID, pageable: Pageable): Page<GroupMessage>
    @EntityGraph(attributePaths = ["reactions", "sender", "replyTo"])
    @Query("SELECT m FROM GroupMessage m WHERE m.group.id = :groupId ORDER BY m.createdAt DESC")
    fun findWithReactionsByGroupId(@Param("groupId") groupId: UUID, pageable: Pageable): Page<GroupMessage>
    fun findTopByGroupIdOrderByCreatedAtDesc(groupId: UUID): GroupMessage?
    fun findByGroupIdAndIsPinnedTrue(groupId: UUID): List<GroupMessage>
    fun findByScheduledAtBeforeAndScheduledAtIsNotNull(time: Instant): List<GroupMessage>
    @Query("SELECT m FROM GroupMessage m WHERE m.group.id = :groupId AND (:query IS NULL OR LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.mediaUrl) LIKE LOWER(CONCAT('%', :query, '%'))) AND m.type IN :types")
    fun searchMessages(@Param("groupId") groupId: UUID, @Param("query") query: String?, @Param("types") types: List<MessageType>, pageable: Pageable): Page<GroupMessage>
    @Query("SELECT m FROM GroupMessage m WHERE m.group.id = :groupId AND (m.type IN :types OR (:includeLinks = true AND m.type = 'TEXT' AND (LOWER(m.content) LIKE '%http%' OR LOWER(m.content) LIKE '%www.%' OR LOWER(m.content) LIKE '%.com%' OR LOWER(m.content) LIKE '%.ir%' OR LOWER(m.content) LIKE '%.net%' OR LOWER(m.content) LIKE '%.org%' OR LOWER(m.content) LIKE '%.info%' OR LOWER(m.content) LIKE '%.io%' OR LOWER(m.content) LIKE '%.co%' OR LOWER(m.content) LIKE '%.me%'))) ORDER BY m.createdAt DESC")
    fun findSharedContent(@Param("groupId") groupId: UUID, @Param("types") types: List<MessageType>, @Param("includeLinks") includeLinks: Boolean, pageable: Pageable): Page<GroupMessage>
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface ChannelRepository : JpaRepository<Channel, UUID> {
    @Query("SELECT c FROM Channel c JOIN ChannelSubscriber cs ON cs.channel.id = c.id WHERE cs.user.id = :userId")
    fun findBySubscriberId(@Param("userId") userId: UUID, pageable: Pageable): Page<Channel>
    fun findByIsPublicTrue(pageable: Pageable): Page<Channel>
    @Query("SELECT c FROM Channel c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchByName(@Param("query") query: String, pageable: Pageable): Page<Channel>
    fun findByInviteLink(inviteLink: String): Channel?
    fun findByPublicId(publicId: String): Channel? // For public channel ID lookups
    fun findByOwnerId(ownerId: UUID): List<Channel>
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Subscriber Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface ChannelSubscriberRepository : JpaRepository<ChannelSubscriber, UUID> {
    fun findByChannelIdAndUserId(channelId: UUID, userId: UUID): ChannelSubscriber?
    fun findByChannelId(channelId: UUID): List<ChannelSubscriber>
    fun existsByChannelIdAndUserId(channelId: UUID, userId: UUID): Boolean
    fun countByChannelId(channelId: UUID): Long
    fun deleteByChannelIdAndUserId(channelId: UUID, userId: UUID)
    @Query("SELECT cs FROM ChannelSubscriber cs WHERE cs.channel.id = :channelId AND cs.isAdmin = true")
    fun findAdminsByChannelId(@Param("channelId") channelId: UUID): List<ChannelSubscriber>
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Post Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface ChannelPostRepository : JpaRepository<ChannelPost, UUID> {
    fun findByChannelIdOrderByCreatedAtDesc(channelId: UUID, pageable: Pageable): Page<ChannelPost>
    @EntityGraph(attributePaths = ["reactions", "poll"])
    @Query("SELECT p FROM ChannelPost p WHERE p.channel.id = :channelId ORDER BY p.createdAt DESC")
    fun findWithReactionsByChannelId(@Param("channelId") channelId: UUID, pageable: Pageable): Page<ChannelPost>
    fun findTopByChannelIdOrderByCreatedAtDesc(channelId: UUID): ChannelPost?
    fun findByChannelIdAndIsPinnedTrue(channelId: UUID): List<ChannelPost>
    fun findByScheduledAtBeforeAndScheduledAtIsNotNull(time: Instant): List<ChannelPost>
    @Query("SELECT p FROM ChannelPost p WHERE p.channel.id = :channelId AND (:query IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.mediaUrl) LIKE LOWER(CONCAT('%', :query, '%'))) AND p.type IN :types")
    fun searchPosts(@Param("channelId") channelId: UUID, @Param("query") query: String?, @Param("types") types: List<MessageType>, pageable: Pageable): Page<ChannelPost>
    @Query("SELECT p FROM ChannelPost p WHERE p.channel.id = :channelId AND (p.type IN :types OR (:includeLinks = true AND p.type = 'TEXT' AND (LOWER(p.content) LIKE '%http%' OR LOWER(p.content) LIKE '%www.%' OR LOWER(p.content) LIKE '%.com%' OR LOWER(p.content) LIKE '%.ir%' OR LOWER(p.content) LIKE '%.net%' OR LOWER(p.content) LIKE '%.org%' OR LOWER(p.content) LIKE '%.info%' OR LOWER(p.content) LIKE '%.io%' OR LOWER(p.content) LIKE '%.co%' OR LOWER(p.content) LIKE '%.me%'))) ORDER BY p.createdAt DESC")
    fun findSharedContent(@Param("channelId") channelId: UUID, @Param("types") types: List<MessageType>, @Param("includeLinks") includeLinks: Boolean, pageable: Pageable): Page<ChannelPost>
}

@Repository
interface ChannelPostReactionRepository : JpaRepository<ChannelPostReaction, UUID> {
    fun findByPostIdAndUserId(postId: UUID, userId: UUID): ChannelPostReaction?
}

@Repository
interface ChannelPostCommentRepository : JpaRepository<ChannelPostComment, UUID> {
    fun findByPostIdOrderByCreatedAtAsc(postId: UUID, pageable: Pageable): Page<ChannelPostComment>
    fun countByPostId(postId: UUID): Long
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔐 OTP Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface OtpCodeRepository : JpaRepository<OtpCode, UUID> {
    fun findByPhoneNumberAndIsUsedFalseOrderByCreatedAtDesc(phoneNumber: String): OtpCode?
    fun findByPhoneNumberAndCodeAndIsUsedFalse(phoneNumber: String, code: String): OtpCode?
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔄 Refresh Token Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByTokenAndIsRevokedFalse(token: String): RefreshToken?
    fun findByUserIdAndIsRevokedFalse(userId: UUID): List<RefreshToken>
}

@Repository
interface MessageReactionRepository : JpaRepository<MessageReaction, UUID> {
    fun findByMessageIdAndUserId(messageId: UUID, userId: UUID): MessageReaction?
}

@Repository
interface GroupMessageReactionRepository : JpaRepository<GroupMessageReaction, UUID> {
    fun findByMessageIdAndUserId(messageId: UUID, userId: UUID): GroupMessageReaction?
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Story Reply Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface StoryReplyRepository : JpaRepository<StoryReply, UUID> {
    fun findByStoryIdOrderByCreatedAtDesc(storyId: UUID): List<StoryReply>
    fun countByStoryId(storyId: UUID): Long
}

