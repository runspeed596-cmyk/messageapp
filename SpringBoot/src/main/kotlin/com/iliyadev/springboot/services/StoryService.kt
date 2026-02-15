package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class StoryService(
    private val storyRepository: StoryRepository,
    private val storyViewRepository: StoryViewRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val userService: UserService,
    private val groupRepository: GroupRepository,
    private val channelRepository: ChannelRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val channelSubscriberRepository: ChannelSubscriberRepository,
    private val storyReplyRepository: StoryReplyRepository,
    private val chatService: ChatService,
    private val messageService: MessageService
) {

    @Transactional
    fun postStory(
        userId: UUID,
        mediaUrl: String,
        type: StoryType,
        caption: String?,
        duration: Int
    ): Story {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        
        // Check limit: Max 1 active story for non-premium users
        val activeStories = storyRepository.findActiveStoriesByUser(user, Instant.now())
        if (!user.isPremium && activeStories.size >= 1) {
            throw IllegalArgumentException("Story limit reached. Upgrade to Premium to post more.")
        }

        val story = Story(
            user = user,
            mediaUrl = mediaUrl,
            type = type,
            caption = caption,
            durationSeconds = duration,
            createdAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(24 * 3600)
        )
        
        return storyRepository.save(story)
    }

    @Transactional(readOnly = true)
    fun getStoriesFeed(currentUserId: UUID): List<StoryUserDto> {
        val currentUser = userRepository.findById(currentUserId).orElseThrow { IllegalArgumentException("User not found") }
        val now = Instant.now()
        
        // 1. Get contacts (users with direct chats)
        // In a real app, you might have a dedicated "Contacts" table. 
        // Here we reuse chat participants as contacts.
        // We find all private chats involving the user.
        val chats = chatRepository.findByParticipantsContainingAndType(currentUser, ChatType.PRIVATE, org.springframework.data.domain.Pageable.unpaged())
        
        val contactIds = chats.flatMap { chat -> 
            chat.participants.map { it.id!! }
        }.filter { it != currentUserId }.toSet()
        
        // Provide the user object for those IDs (optimization: fetch all at once)
        val contacts = userRepository.findAllById(contactIds)
        
        // 2. Also include My Stories
        val usersToCheck = contacts + currentUser
        
        // 3. Fetch active stories for all these users
        val allStories = storyRepository.findActiveStoriesByUsers(usersToCheck, now)
        
        // 4. Group by user and map to DTO
        return allStories.groupBy { it.user!! }.map { (user, stories) ->
            StoryUserDto(
                userId = user.id!!,
                username = user.username,
                displayName = if (user.id == currentUserId) "استوری شما" else user.displayName,
                avatarUrl = user.avatarUrl,
                stories = stories.map { it.toDto(currentUserId) },
                isCurrentUser = user.id == currentUserId
            )
        }
    }

    @Transactional
    fun markAsViewed(userId: UUID, storyId: UUID) {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        val story = storyRepository.findById(storyId).orElseThrow { IllegalArgumentException("Story not found") }
        
        // Don't count self-views? Usually apps do count them or just ignore saving View record but show "0 views" to others.
        // Let's record it so we track "read" status (gray ring).
        
        if (!storyViewRepository.existsByStoryAndUser(story, user)) {
            val view = StoryView(
                story = story,
                user = user,
                viewedAt = Instant.now()
            )
            storyViewRepository.save(view)
        }
    }

    @Transactional(readOnly = true)
    fun getStoryViews(requesterId: UUID, storyId: UUID): List<StoryViewDto> {
        val story = storyRepository.findById(storyId).orElseThrow { IllegalArgumentException("Story not found") }
        
        var canView = false
        if (story.group != null) {
            // Check group admin rights
             val member = groupMemberRepository.findByGroupIdAndUserId(story.group!!.id!!, requesterId)
             if (member != null && (member.role == MemberRole.ADMIN || member.role == MemberRole.OWNER || member.canPostStory)) {
                 canView = true
             }
        } else if (story.channel != null) {
            // Check channel admin rights
            if (story.channel!!.owner?.id == requesterId) {
                canView = true
            } else {
                val sub = channelSubscriberRepository.findByChannelIdAndUserId(story.channel!!.id!!, requesterId)
                if (sub != null && sub.isAdmin) { // TODO: Granular permissions for channel
                    canView = true
                }
            }
        } else {
             // User story
             if (story.user?.id == requesterId) {
                 canView = true
             }
        }

        if (!canView) {
            throw IllegalArgumentException("Access denied")
        }
        
        return storyViewRepository.findByStoryOrderByViewedAtDesc(story).map { it.toDto() }
    }
    @Transactional
    fun deleteStory(userId: UUID, storyId: UUID) {
        val story = storyRepository.findById(storyId).orElseThrow { IllegalArgumentException("Story not found") }
        
        var canDelete = false
        if (story.group != null) {
             val member = groupMemberRepository.findByGroupIdAndUserId(story.group!!.id!!, userId)
             if (member != null && (member.role == MemberRole.ADMIN || member.role == MemberRole.OWNER)) {
                 canDelete = true
             }
        } else if (story.channel != null) {
             if (story.channel!!.owner?.id == userId) {
                canDelete = true
            } else {
                val sub = channelSubscriberRepository.findByChannelIdAndUserId(story.channel!!.id!!, userId)
                if (sub != null && sub.isAdmin) {
                    canDelete = true
                }
            }
        } else {
             if (story.user?.id == userId) {
                 canDelete = true
             }
        }

        if (!canDelete) {
            throw IllegalArgumentException("Access denied")
        }
        
        // Optional: Delete physical file if FileUploadService supports it
        // fileUploadService.deleteFile(story.mediaUrl) 
        
        // Delete related views (if not cascaded automatically, usually JPA handles it if configured, or we delete here)
        storyViewRepository.deleteByStory(story)
        
        storyRepository.delete(story)
    }

    @Transactional
    fun postGroupStory(userId: UUID, groupId: UUID, mediaUrl: String, type: StoryType, caption: String?, duration: Int): Story {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        val group = groupRepository.findById(groupId).orElseThrow { IllegalArgumentException("Group not found") }
        
        // Check permissions: Owner or Admin
        val member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId) 
            ?: throw IllegalArgumentException("Not a member of this group")
        
        if (member.role == MemberRole.MEMBER && !member.canPostStory) {
             throw IllegalArgumentException("Only admins can post stories")
        }
        
        // Check limit: Max 1 active story
        val activeCount = storyRepository.countActiveStoriesByGroup(group, Instant.now())
        if (activeCount >= 1) {
            throw IllegalArgumentException("Group story limit reached (Max 1)")
        }

        val story = Story(
            user = user,
            group = group,
            mediaUrl = mediaUrl,
            type = type,
            caption = caption,
            durationSeconds = duration,
            createdAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(24 * 3600)
        )
        return storyRepository.save(story)
    }

    @Transactional
    fun postChannelStory(userId: UUID, channelId: UUID, mediaUrl: String, type: StoryType, caption: String?, duration: Int): Story {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        val channel = channelRepository.findById(channelId).orElseThrow { IllegalArgumentException("Channel not found") }

        val subscriber = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
        val isOwner = channel.owner?.id == userId
        
        if (!isOwner) {
             if (subscriber == null || (!subscriber.isAdmin && !subscriber.canPostStory)) {
                 throw IllegalArgumentException("Only admins can post stories")
             }
        }
        
        val activeCount = storyRepository.countActiveStoriesByChannel(channel, Instant.now())
        if (activeCount >= 1) {
            throw IllegalArgumentException("Channel story limit reached (Max 1)")
        }

        val story = Story(
            user = user,
            channel = channel,
            mediaUrl = mediaUrl,
            type = type,
            caption = caption,
            durationSeconds = duration,
            createdAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(24 * 3600)
        )
        return storyRepository.save(story)
    }
    
    @Transactional(readOnly = true)
    fun getGroupStoriesFeed(userId: UUID): List<StoryUserDto> {
        val groupsPage = groupRepository.findByMemberId(userId, org.springframework.data.domain.Pageable.unpaged())
        val groups = groupsPage.content
        
        val activeStories = storyRepository.findActiveStoriesByGroups(groups, Instant.now())
        
         return activeStories.groupBy { it.group!! }.map { (group, stories) ->
             // Check permission
             val member = groupMemberRepository.findByGroupIdAndUserId(group.id!!, userId)
             val canManage = member != null && (member.role == MemberRole.ADMIN || member.role == MemberRole.OWNER || member.canPostStory)
             
             StoryUserDto(
                 userId = group.id!!,
                 username = group.id.toString(),
                 displayName = group.name,
                 avatarUrl = group.avatarUrl,
                 stories = stories.map { it.toDto(userId) },
                 isCurrentUser = canManage
             )
         }
    }

    @Transactional(readOnly = true)
    fun getChannelStoriesFeed(userId: UUID): List<StoryUserDto> {
         val channelsPage = channelRepository.findBySubscriberId(userId, org.springframework.data.domain.Pageable.unpaged())
         val channels = channelsPage.content
         
         val activeStories = storyRepository.findActiveStoriesByChannels(channels, Instant.now())
         
         return activeStories.groupBy { it.channel!! }.map { (channel, stories) ->
             val isOwner = channel.owner?.id == userId
             val sub = if (isOwner) null else channelSubscriberRepository.findByChannelIdAndUserId(channel.id!!, userId)
             val canManage = isOwner || (sub != null && sub.isAdmin)

             StoryUserDto(
                 userId = channel.id!!,
                 username = channel.publicId ?: channel.id.toString(),
                 displayName = channel.name,
                 avatarUrl = channel.avatarUrl,
                 stories = stories.map { it.toDto(userId) },
                 isCurrentUser = canManage
             )
          }
     }

    @Transactional
    fun replyToStory(userId: UUID, storyId: UUID, content: String): StoryReplyDto {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        val story = storyRepository.findById(storyId).orElseThrow { IllegalArgumentException("Story not found") }
        val reply = StoryReply(
            story = story,
            user = user,
            content = content,
            createdAt = Instant.now()
        )
        val saved = storyReplyRepository.save(reply)
        // Send the reply as a private chat message to the story owner
        val storyOwnerId = story.user?.id
        if (storyOwnerId != null && storyOwnerId != userId) {
            try {
                val chatDto = chatService.createPrivateChat(userId, storyOwnerId)
                if (chatDto != null) {
                    val msgContent = "\uD83D\uDCE9 پاسخ استوری:\n$content"
                    val sendRequest = SendMessageRequest(content = msgContent)
                    messageService.sendMessage(chatDto.id, userId, sendRequest)
                }
            } catch (e: Exception) {
                // Log but don't fail the reply itself
                println("WARN: Failed to send story reply as chat message: ${e.message}")
            }
        }
        return saved.toDto()
    }

    @Transactional(readOnly = true)
    fun getStoryReplies(storyId: UUID): List<StoryReplyDto> {
        return storyReplyRepository.findByStoryIdOrderByCreatedAtDesc(storyId).map { it.toDto() }
    }
}
