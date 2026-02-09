package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import com.iliyadev.springboot.websocket.WebSocketMessageHandler
import com.iliyadev.springboot.websocket.WsMessage
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val groupMessageRepository: GroupMessageRepository,
    private val userRepository: UserRepository,
    private val groupMessageReactionRepository: GroupMessageReactionRepository,
    private val webSocketMessageHandler: WebSocketMessageHandler,
    private val pollRepository: PollRepository
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(GroupService::class.java)
    fun getGroupsForUser(userId: UUID, page: Int, size: Int): GroupListResponse {
        val pageable = PageRequest.of(page, size)
        val groups = groupRepository.findByMemberId(userId, pageable)
        return GroupListResponse(
            groups = groups.content.map { group -> groupToDto(group, userId) },
            totalCount = groups.totalElements.toInt()
        )
    }
    @Transactional
    fun createGroup(userId: UUID, request: CreateGroupRequest): GroupDto? {
        val creator = userRepository.findById(userId).orElse(null) ?: return null
        val inviteLink = generateInviteLink()
        val group = Group().apply {
            name = request.name
            description = request.description
            isPublic = false // Force private as per requirement
            avatarUrl = request.avatarUrl
            this.inviteLink = inviteLink
            createdBy = creator
            createdAt = Instant.now()
        }
        val savedGroup = groupRepository.save(group)
        val ownerMember = GroupMember().apply {
            this.group = savedGroup
            user = creator
            role = MemberRole.OWNER
            joinedAt = Instant.now()
        }
        groupMemberRepository.save(ownerMember)
        request.memberIds.forEach { memberId ->
            if (memberId != userId) {
                val member = userRepository.findById(memberId).orElse(null)
                if (member != null) {
                    val groupMember = GroupMember().apply {
                        this.group = savedGroup
                        user = member
                        role = MemberRole.MEMBER
                        joinedAt = Instant.now()
                    }
                    groupMemberRepository.save(groupMember)
                }
            }
        }
        return groupToDto(savedGroup, userId)
    }
    fun getGroupById(groupId: UUID, userId: UUID): GroupDto? {
        val group = groupRepository.findById(groupId).orElse(null) ?: return null
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        if (membership == null && !group.isPublic) return null
        return groupToDto(group, userId)
    }
    @Transactional
    fun updateGroup(groupId: UUID, userId: UUID, request: UpdateGroupRequest): GroupDto? {
        val group = groupRepository.findById(groupId).orElse(null) ?: return null
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        if (membership == null) return null
        // Allow updating info if role is OWNER/ADMIN OR if allowMembersToEditInfo is true
        val canEdit = membership.role == MemberRole.OWNER || 
                      membership.role == MemberRole.ADMIN || 
                      group.allowMembersToEditInfo
        if (!canEdit) return null
        if (request.name != null) group.name = request.name
        if (request.description != null) group.description = request.description
        if (request.isPublic != null) group.isPublic = request.isPublic
        if (request.avatarUrl != null) group.avatarUrl = request.avatarUrl
        val savedGroup = groupRepository.save(group)
        return groupToDto(savedGroup, userId)
    }
    @Transactional
    fun deleteGroup(groupId: UUID, userId: UUID): Boolean {
        val group = groupRepository.findById(groupId).orElse(null) ?: return false
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        if (membership == null || membership.role != MemberRole.OWNER) return false
        groupRepository.delete(group)
        return true
    }
    @Transactional
    fun addMembers(groupId: UUID, userId: UUID, memberIds: List<UUID>): Boolean {
        val group = groupRepository.findById(groupId).orElse(null) ?: return false
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        if (membership == null || membership.role == MemberRole.MEMBER) return false
        
        val addedMembers = mutableListOf<User>()
        memberIds.forEach { memberId ->
            if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, memberId)) {
                val user = userRepository.findById(memberId).orElse(null)
                if (user != null) {
                    val member = GroupMember().apply {
                        this.group = group
                        this.user = user
                        role = MemberRole.MEMBER
                        joinedAt = Instant.now()
                    }
                    groupMemberRepository.save(member)
                    addedMembers.add(user)
                }
            }
        }
        
        // Broadcast member additions via WebSocket for real-time updates
        if (addedMembers.isNotEmpty()) {
            val newMemberCount = groupMemberRepository.countByGroupId(groupId).toInt()
            val allMemberIds = groupMemberRepository.findByGroupId(groupId)
                .mapNotNull { it.user?.id }
            
            addedMembers.forEach { addedUser ->
                val event = com.iliyadev.springboot.websocket.WsGroupMemberEvent(
                    event = "MEMBER_ADDED",
                    groupId = groupId,
                    memberId = addedUser.id!!,
                    memberName = addedUser.displayName,
                    memberAvatar = addedUser.avatarUrl,
                    role = MemberRole.MEMBER.name,
                    newMemberCount = newMemberCount
                )
                webSocketMessageHandler.broadcastGroupMemberUpdate(groupId, event, allMemberIds)
            }
        }
        
        return true
    }
    @Transactional
    fun removeMember(groupId: UUID, userId: UUID, targetUserId: UUID): Boolean {
        val isSelfRemoval = userId == targetUserId
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        
        if (!isSelfRemoval) {
            if (membership == null || membership.role == MemberRole.MEMBER) return false
        }
        
        val targetMembership = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
            ?: return false
            
        // Cannot remove Owner (unless deleting group, handled elsewhere)
        if (targetMembership.role == MemberRole.OWNER) return false
        
        // If trying to remove someone else, check permissions
        if (!isSelfRemoval && targetMembership.role == MemberRole.ADMIN && membership?.role != MemberRole.OWNER) {
             return false // Admin can only be removed by Owner
        }

        // Get member info before deletion for broadcast
        val removedUser = targetMembership.user
        val remainingMemberIds = groupMemberRepository.findByGroupId(groupId)
            .mapNotNull { it.user?.id }
            .filter { it != targetUserId }

        groupMemberRepository.delete(targetMembership)
        
        // Broadcast member removal via WebSocket for real-time updates
        if (removedUser != null) {
            val newMemberCount = groupMemberRepository.countByGroupId(groupId).toInt()
            val event = com.iliyadev.springboot.websocket.WsGroupMemberEvent(
                event = "MEMBER_REMOVED",
                groupId = groupId,
                memberId = removedUser.id!!,
                memberName = removedUser.displayName,
                memberAvatar = removedUser.avatarUrl,
                role = null,
                newMemberCount = newMemberCount
            )
            webSocketMessageHandler.broadcastGroupMemberUpdate(groupId, event, remainingMemberIds)
        }
        
        return true
    }
    @Transactional
    fun changeRole(groupId: UUID, userId: UUID, targetUserId: UUID, request: ChangeRoleRequest): Boolean {
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        if (membership == null || membership.role != MemberRole.OWNER) return false
        val targetMembership = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
            ?: return false
        if (request.role == MemberRole.OWNER) return false
        
        targetMembership.role = request.role
        // Apply permissions if role is ADMIN
        if (request.role == MemberRole.ADMIN) {
            targetMembership.canEditInfo = request.canEditInfo
            targetMembership.canPostStory = request.canPostStory
            targetMembership.canAddMembers = request.canAddMembers
            targetMembership.canRemoveMembers = request.canRemoveMembers
        } else {
            // Reset permissions if not Admin
            targetMembership.canEditInfo = false
            targetMembership.canPostStory = false
            targetMembership.canAddMembers = false
            targetMembership.canRemoveMembers = false
        }
        
        groupMemberRepository.save(targetMembership)
        
        // Broadcast role change
        val allMemberIds = groupMemberRepository.findByGroupId(groupId).mapNotNull { it.user?.id }
        val event = com.iliyadev.springboot.websocket.WsGroupMemberEvent(
            event = "ROLE_CHANGED",
            groupId = groupId,
            memberId = targetUserId,
            memberName = targetMembership.user?.displayName ?: "",
            memberAvatar = targetMembership.user?.avatarUrl,
            role = request.role.name,
            newMemberCount = groupMemberRepository.countByGroupId(groupId).toInt()
        )
        webSocketMessageHandler.broadcastGroupMemberUpdate(groupId, event, allMemberIds)
        
        return true
    }
    fun getGroupMembers(groupId: UUID, userId: UUID): List<GroupMemberDto>? {
        val group = groupRepository.findById(groupId).orElse(null) ?: return null
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        if (membership == null && !group.isPublic) return null
        return groupMemberRepository.findByGroupId(groupId).map { member ->
            GroupMemberDto(
                user = member.user!!.toDto(),
                role = member.role,
                joinedAt = member.joinedAt,
                canEditInfo = member.canEditInfo,
                canPostStory = member.canPostStory,
                canAddMembers = member.canAddMembers,
                canRemoveMembers = member.canRemoveMembers
            )
        }
    }
    fun getGroupMessages(groupId: UUID, userId: UUID, page: Int, size: Int): GroupMessageListResponse {
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            ?: throw IllegalAccessException("شما عضو این گروه نیستید")
        val pageable = PageRequest.of(page, size)
        val messages = groupMessageRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable)
        return GroupMessageListResponse(
            messages = messages.content.map { msg -> msg.toDto(userId) },
            totalCount = messages.totalElements.toInt(),
            hasMore = messages.hasNext()
        )
    }

    fun searchGroupMessages(groupId: UUID, userId: UUID, query: String?, types: List<MessageType>?, page: Int, size: Int): GroupMessageListResponse {
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            ?: throw IllegalAccessException("شما عضو این گروه نیستید")
        val pageable = PageRequest.of(page, size)
        
        val searchTypes = if (types.isNullOrEmpty()) MessageType.values().toList() else types

        val messages = groupMessageRepository.searchMessages(groupId, query, searchTypes, pageable)
        return GroupMessageListResponse(
            messages = messages.content.map { msg -> msg.toDto(userId) },
            totalCount = messages.totalElements.toInt(),
            hasMore = messages.hasNext()
        )
    }
    @Transactional
    fun sendGroupMessage(groupId: UUID, userId: UUID, request: SendGroupMessageRequest): GroupMessageDto? {
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId) ?: return null
        val group = groupRepository.findById(groupId).orElse(null) ?: return null
        // Check if member has permission to send messages
        if (membership.role == MemberRole.MEMBER && !group.allowMembersToSendMessages) {
            return null
        }
        val sender = userRepository.findById(userId).orElse(null) ?: return null
        var replyTo: GroupMessage? = null
        if (request.replyToMessageId != null) {
            replyTo = groupMessageRepository.findById(request.replyToMessageId).orElse(null)
        }
        
        // Look up poll if pollId is provided
        var poll: Poll? = null
        if (request.pollId != null) {
            poll = pollRepository.findById(request.pollId).orElse(null)
        }
        
        val message = GroupMessage().apply {
            this.group = group
            this.sender = sender
            type = request.type
            content = request.content
            mediaUrl = request.mediaUrl
            this.replyTo = replyTo
            this.poll = poll
            createdAt = Instant.now()
            // Null-safe amplitudes handling
            if (!request.amplitudes.isNullOrEmpty()) {
                this.amplitudes = request.amplitudes.toMutableList()
            }
        }
        val savedMessage = groupMessageRepository.save(message)
        
        // Broadcast message to all group members via WebSocket for real-time delivery
        logger.info("📤 Broadcasting group message ${savedMessage.id} to group $groupId")
        val wsMessage = WsMessage(
            id = savedMessage.id!!,
            chatId = groupId,
            senderId = sender.id!!,
            senderName = sender.displayName,
            senderAvatar = sender.avatarUrl,
            content = savedMessage.content,
            type = savedMessage.type,
            mediaUrl = savedMessage.mediaUrl,   // CRITICAL: Include media URL for audio/video/image/file
            poll = savedMessage.poll?.toDto(userId),   // CRITICAL: Include poll for poll messages
            amplitudes = savedMessage.amplitudes, // Include waveform for voice/audio
            timestamp = savedMessage.createdAt,
            replyToMessageId = replyTo?.id,
            replyToSenderName = replyTo?.sender?.displayName,
            replyToContent = replyTo?.content
        )
        
        val memberIds = groupMemberRepository.findByGroupId(groupId)
            .mapNotNull { it.user?.id }
            .filter { it != userId }  // Exclude sender
        
        webSocketMessageHandler.broadcastGroupMessage(groupId, wsMessage, memberIds)
        
        return savedMessage.toDto(userId)
    }

    @Transactional
    fun editGroupMessage(groupId: UUID, userId: UUID, messageId: UUID, content: String): GroupMessageDto? {
        val message = groupMessageRepository.findById(messageId).orElse(null) ?: return null
        if (message.group?.id != groupId) return null
        
        // Only the sender can edit their own message
        if (message.sender?.id != userId) return null
        
        message.content = content
        message.isEdited = true
        message.editedAt = Instant.now()
        val savedMessage = groupMessageRepository.save(message)
        return savedMessage.toDto(userId)
    }

    @Transactional
    fun deleteGroupMessage(groupId: UUID, userId: UUID, messageId: UUID, deleteForEveryone: Boolean): Boolean {
        val message = groupMessageRepository.findById(messageId).orElse(null) ?: return false
        if (message.group?.id != groupId) return false
        
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        
        // Permission check:
        // - Message sender can delete their own message
        // - OWNER or ADMIN can delete any message
        val isSender = message.sender?.id == userId
        val isOwnerOrAdmin = membership?.role == MemberRole.OWNER || membership?.role == MemberRole.ADMIN
        
        if (!isSender && !isOwnerOrAdmin) return false
        
        if (deleteForEveryone) {
            groupMessageRepository.delete(message)
        } else {
            // For "delete for me", we would need a soft delete mechanism
            // For now, treat same as delete for everyone
            groupMessageRepository.delete(message)
        }
        return true
    }

    @Transactional
    fun updateGroupSettings(groupId: UUID, userId: UUID, request: UpdateGroupSettingsRequest): GroupDto? {
        val group = groupRepository.findById(groupId).orElse(null) ?: return null
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        // Only OWNER and ADMIN can update settings
        if (membership == null || membership.role == MemberRole.MEMBER) return null
        if (request.allowMembersToSendMessages != null) {
            group.allowMembersToSendMessages = request.allowMembersToSendMessages
        }
        if (request.allowMembersToEditInfo != null) {
            group.allowMembersToEditInfo = request.allowMembersToEditInfo
        }
        val savedGroup = groupRepository.save(group)
        return groupToDto(savedGroup, userId)
    }
    @Transactional
    fun toggleInviteLink(groupId: UUID, userId: UUID, enabled: Boolean): InviteLinkResponse? {
        val group = groupRepository.findById(groupId).orElse(null) ?: return null
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        // Only OWNER and ADMIN can manage invite link
        if (membership == null || membership.role == MemberRole.MEMBER) return null
        group.isInviteLinkEnabled = enabled
        val savedGroup = groupRepository.save(group)
        return InviteLinkResponse(
            inviteLink = if (enabled) savedGroup.inviteLink else null,
            isEnabled = savedGroup.isInviteLinkEnabled
        )
    }
    @Transactional
    fun regenerateInviteLink(groupId: UUID, userId: UUID): InviteLinkResponse? {
        val group = groupRepository.findById(groupId).orElse(null) ?: return null
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        // Only OWNER and ADMIN can regenerate invite link
        if (membership == null || membership.role == MemberRole.MEMBER) return null
        group.inviteLink = generateInviteLink()
        group.isInviteLinkEnabled = true
        val savedGroup = groupRepository.save(group)
        return InviteLinkResponse(
            inviteLink = savedGroup.inviteLink,
            isEnabled = savedGroup.isInviteLinkEnabled
        )
    }
    @Transactional
    fun joinByInviteLink(inviteCode: String, userId: UUID): GroupDto? {
        val inviteLink = "https://msgapp.com/g/$inviteCode"
        val group = groupRepository.findByInviteLink(inviteLink) ?: return null
        if (!group.isInviteLinkEnabled) return null
        // Check if already a member
        if (groupMemberRepository.existsByGroupIdAndUserId(group.id!!, userId)) {
            return groupToDto(group, userId)
        }
        val user = userRepository.findById(userId).orElse(null) ?: return null
        val member = GroupMember().apply {
            this.group = group
            this.user = user
            role = MemberRole.MEMBER
            joinedAt = Instant.now()
        }
        groupMemberRepository.save(member)
        return groupToDto(group, userId)
    }

    @Transactional
    fun reactToGroupMessage(messageId: UUID, userId: UUID, reaction: String?): Boolean {
        val message = groupMessageRepository.findById(messageId).orElse(null) ?: return false
        val group = message.group ?: return false
        
        // Check membership
        val membership = groupMemberRepository.findByGroupIdAndUserId(group.id!!, userId)
        if (membership == null) return false

        val existingReaction = groupMessageReactionRepository.findByMessageIdAndUserId(messageId, userId)
        if (reaction == null) {
            if (existingReaction != null) {
                groupMessageReactionRepository.delete(existingReaction)
            }
        } else {
            if (existingReaction != null) {
                existingReaction.reaction = reaction
                groupMessageReactionRepository.save(existingReaction)
            } else {
                val user = userRepository.findById(userId).orElse(null) ?: return false
                val newReaction = GroupMessageReaction().apply {
                    this.message = message
                    this.user = user
                    this.reaction = reaction
                    this.createdAt = Instant.now()
                }
                groupMessageReactionRepository.save(newReaction)
            }
        }
        
        // TODO: Broadcasting reactions is not implemented yet, but persistence works.
        // Usually we send a WS event like "MESSAGE_UPDATED" or "REACTION_ADDED".
        // For now relying on polling or separate update.
        
        return true
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 🔔 Member Settings (Mute/Pin/Archive)
    // ═══════════════════════════════════════════════════════════════════════════════
    @Transactional
    fun updateMemberSettings(groupId: UUID, userId: UUID, isMuted: Boolean?, isPinned: Boolean?, isArchived: Boolean?): GroupDto? {
        val membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId) ?: return null
        if (isMuted != null) membership.isMuted = isMuted
        if (isPinned != null) membership.isPinned = isPinned
        if (isArchived != null) membership.isArchived = isArchived
        groupMemberRepository.save(membership)
        val group = groupRepository.findById(groupId).orElse(null) ?: return null
        return groupToDto(group, userId)
    }

    private fun groupToDto(group: Group, userId: UUID): GroupDto {
        val memberCount = groupMemberRepository.countByGroupId(group.id!!)
        val myMembership = groupMemberRepository.findByGroupIdAndUserId(group.id!!, userId)
        val lastMessage = groupMessageRepository.findTopByGroupIdOrderByCreatedAtDesc(group.id!!)
        return GroupDto(
            id = group.id!!,
            name = group.name,
            description = group.description,
            avatarUrl = group.avatarUrl,
            memberCount = memberCount.toInt(),
            isPublic = group.isPublic,
            inviteLink = if (group.isInviteLinkEnabled) group.inviteLink else null,
            isInviteLinkEnabled = group.isInviteLinkEnabled,
            allowMembersToSendMessages = group.allowMembersToSendMessages,
            allowMembersToEditInfo = group.allowMembersToEditInfo,
            createdAt = group.createdAt,
            createdBy = group.createdBy?.toDto(),
            myRole = myMembership?.role,
            lastMessage = lastMessage?.toDto(userId),
            unreadCount = 0, // TODO: Implement unread count
            isMuted = myMembership?.isMuted ?: false,
            isPinned = myMembership?.isPinned ?: false,
            isArchived = myMembership?.isArchived ?: false
        )
    }
    private fun generateInviteLink(): String = "https://msgapp.com/g/${UUID.randomUUID().toString().take(8)}"
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class ChannelService(
    private val channelRepository: ChannelRepository,
    private val channelSubscriberRepository: ChannelSubscriberRepository,
    private val channelPostRepository: ChannelPostRepository,
    private val channelPostReactionRepository: ChannelPostReactionRepository,
    private val channelPostCommentRepository: ChannelPostCommentRepository,
    private val userRepository: UserRepository,
    private val webSocketMessageHandler: WebSocketMessageHandler,
    private val pollRepository: PollRepository
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(ChannelService::class.java)
    fun getChannelsForUser(userId: UUID, page: Int, size: Int): ChannelListResponse {
        val pageable = PageRequest.of(page, size)
        val channels = channelRepository.findBySubscriberId(userId, pageable)
        return ChannelListResponse(
            channels = channels.content.map { channel ->
                channelToDto(channel, userId)
            },
            totalCount = channels.totalElements.toInt()
        )
    }
    @Transactional
    fun createChannel(userId: UUID, request: CreateChannelRequest): ChannelDto? {
        val owner = userRepository.findById(userId).orElse(null) ?: return null
        
        // Validate publicId if provided (must be unique and follow format)
        if (request.publicId != null) {
            val normalizedPublicId = request.publicId.lowercase().replace("@", "")
            if (channelRepository.findByPublicId(normalizedPublicId) != null) {
                throw IllegalArgumentException("این شناسه عمومی قبلا استفاده شده است")
            }
        }
        
        val inviteLink = generateInviteLink()
        val channel = Channel().apply {
            name = request.name
            description = request.description
            isPublic = request.isPublic
            publicId = request.publicId?.lowercase()?.replace("@", "")
            this.inviteLink = inviteLink
            this.owner = owner
            avatarUrl = request.avatarUrl
            createdAt = Instant.now()
        }
        val savedChannel = channelRepository.save(channel)
        val ownerSubscription = ChannelSubscriber().apply {
            this.channel = savedChannel
            user = owner
            isAdmin = true
            subscribedAt = Instant.now()
        }
        channelSubscriberRepository.save(ownerSubscription)

        // Add initial members
        request.memberIds.forEach { memberId ->
            if (memberId != userId) {
                val member = userRepository.findById(memberId).orElse(null)
                if (member != null) {
                    val sub = ChannelSubscriber().apply {
                        this.channel = savedChannel
                        this.user = member
                        this.isAdmin = false
                        this.subscribedAt = Instant.now()
                    }
                    channelSubscriberRepository.save(sub)
                }
            }
        }

        return channelToDto(savedChannel, userId)
    }
    fun getChannelById(channelId: UUID, userId: UUID): ChannelDto? {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return null
        if (!channel.isPublic) {
            val subscription = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
            if (subscription == null) return null
        }
        return channelToDto(channel, userId)
    }
    @Transactional
    fun updateChannel(channelId: UUID, userId: UUID, request: UpdateChannelRequest): ChannelDto? {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return null
        if (channel.owner?.id != userId) {
            val subscription = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
            if (subscription == null || !subscription.isAdmin) return null
        }
        if (request.name != null) channel.name = request.name
        if (request.description != null) channel.description = request.description
        if (request.isPublic != null) channel.isPublic = request.isPublic
        if (request.avatarUrl != null) channel.avatarUrl = request.avatarUrl
        
        // Handle publicId update (validate uniqueness)
        if (request.publicId != null) {
            val normalizedPublicId = request.publicId.lowercase().replace("@", "")
            val existingChannel = channelRepository.findByPublicId(normalizedPublicId)
            if (existingChannel != null && existingChannel.id != channelId) {
                throw IllegalArgumentException("این شناسه عمومی قبلا استفاده شده است")
            }
            channel.publicId = normalizedPublicId
        }
        
        val savedChannel = channelRepository.save(channel)
        return channelToDto(savedChannel, userId)
    }
    @Transactional
    fun deleteChannel(channelId: UUID, userId: UUID): Boolean {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return false
        if (channel.owner?.id != userId) return false
        channelRepository.delete(channel)
        return true
    }
    @Transactional
    fun subscribe(channelId: UUID, userId: UUID): Boolean {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return false
        val user = userRepository.findById(userId).orElse(null) ?: return false
        if (channelSubscriberRepository.existsByChannelIdAndUserId(channelId, userId)) return true
        val subscription = ChannelSubscriber().apply {
            this.channel = channel
            this.user = user
            isAdmin = false
            subscribedAt = Instant.now()
        }
        channelSubscriberRepository.save(subscription)
        return true
    }
    @Transactional
    fun unsubscribe(channelId: UUID, userId: UUID): Boolean {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return false
        if (channel.owner?.id == userId) return false
        channelSubscriberRepository.deleteByChannelIdAndUserId(channelId, userId)
        return true
    }
    fun getPosts(channelId: UUID, userId: UUID, page: Int, size: Int): PostListResponse {
        val channel = channelRepository.findById(channelId).orElse(null)
            ?: throw IllegalArgumentException("کانال یافت نشد")
        if (!channel.isPublic) {
            val subscription = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
            if (subscription == null) throw IllegalAccessException("شما عضو این کانال نیستید")
        }
        val pageable = PageRequest.of(page, size)
        val posts = channelPostRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable)
        return PostListResponse(
            posts = posts.content.map { it.toDto(userId) },
            totalCount = posts.totalElements.toInt(),
            hasMore = posts.hasNext()
        )
    }

    fun searchChannelPosts(channelId: UUID, userId: UUID, query: String?, types: List<MessageType>?, page: Int, size: Int): PostListResponse {
        val channel = channelRepository.findById(channelId).orElse(null)
            ?: throw IllegalArgumentException("کانال یافت نشد")
        if (!channel.isPublic) {
            val subscription = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
            if (subscription == null) throw IllegalAccessException("شما عضو این کانال نیستید")
        }
        val pageable = PageRequest.of(page, size)
        
        val searchTypes = if (types.isNullOrEmpty()) MessageType.values().toList() else types

        val posts = channelPostRepository.searchPosts(channelId, query, searchTypes, pageable)
        return PostListResponse(
            posts = posts.content.map { it.toDto(userId) },
            totalCount = posts.totalElements.toInt(),
            hasMore = posts.hasNext()
        )
    }
    @Transactional
    fun createPost(channelId: UUID, userId: UUID, request: CreatePostRequest): ChannelPostDto? {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return null
        if (channel.owner?.id != userId) {
            val subscription = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
            if (subscription == null || !subscription.isAdmin) return null
        }
        
        // Look up poll if pollId is provided (proper way to handle polls)
        var poll: Poll? = null
        if (request.pollId != null) {
            poll = pollRepository.findById(request.pollId).orElse(null)
            logger.info("📊 Found poll for channel post: pollId=${request.pollId}, poll=${poll?.question}")
        }
        
        val post = ChannelPost().apply {
            this.channel = channel
            type = try { MessageType.valueOf(request.type) } catch (e: Exception) { MessageType.TEXT }
            content = request.content
            
            // Poll Handling - prefer pollId lookup over mediaUrl JSON parsing
            if (poll != null) {
                this.poll = poll
                this.mediaUrl = null // Clear mediaUrl when using poll entity
                logger.info("📊 Attached poll to channel post: ${poll.question}")
            } else if (type == MessageType.POLL && request.mediaUrl != null) {
                // Fallback: Try parsing poll from mediaUrl JSON (legacy support)
                try {
                     val pollDto = com.google.gson.Gson().fromJson(request.mediaUrl, PollDto::class.java)
                     val newPoll = Poll().apply {
                         question = pollDto.question
                         isMultipleChoice = pollDto.isMultipleChoice
                         isAnonymous = pollDto.isAnonymous
                         createdAt = Instant.now()
                         val creator = userRepository.findById(userId).orElse(null)
                         this.creator = creator
                     }
                     
                     val opts = pollDto.options.map { optDto ->
                         PollOption().apply {
                             text = optDto.text
                             voteCount = 0
                             this.poll = newPoll
                         }
                     }.toMutableList()
                     
                     newPoll.options = opts
                     this.poll = newPoll
                     this.mediaUrl = null
                } catch (e: Exception) {
                    logger.warn("⚠️ Failed to parse poll from mediaUrl: ${e.message}")
                    this.mediaUrl = request.mediaUrl
                }
            } else {
                this.mediaUrl = request.mediaUrl
            }
            commentsEnabled = request.commentsEnabled
            createdAt = Instant.now()
            // Null-safe amplitudes handling
            if (!request.amplitudes.isNullOrEmpty()) {
                this.amplitudes = request.amplitudes.toMutableList()
            }
        }
        val savedPost = channelPostRepository.save(post)
        
        // Broadcast post to all channel subscribers via WebSocket for real-time delivery
        logger.info("📤 Broadcasting channel post ${savedPost.id} to channel $channelId")
        val subscriberIds = channelSubscriberRepository.findByChannelId(channelId)
            .mapNotNull { it.user?.id }
        
        webSocketMessageHandler.broadcastChannelPost(channelId, savedPost.toDto(null), subscriberIds)
        
        return savedPost.toDto(userId)
    }

    @Transactional
    fun editPost(channelId: UUID, userId: UUID, postId: UUID, content: String): ChannelPostDto? {
        val post = channelPostRepository.findById(postId).orElse(null) ?: return null
        val channel = post.channel ?: return null
        if (channel.id != channelId) return null
        
        // Check permissions: Owner or Admin
        if (channel.owner?.id != userId) {
            val subscriber = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
            if (subscriber == null || !subscriber.isAdmin) return null
        }
        
        post.content = content
        post.editedAt = Instant.now()
        val savedPost = channelPostRepository.save(post)
        return savedPost.toDto(userId)
    }
    @Transactional
    fun incrementViewCount(postId: UUID): Boolean {
        val post = channelPostRepository.findById(postId).orElse(null) ?: return false
        post.viewCount = post.viewCount + 1
        channelPostRepository.save(post)
        return true
    }
    fun searchPublicChannels(query: String, page: Int, size: Int): ChannelListResponse {
        val pageable = PageRequest.of(page, size)
        val channels = channelRepository.searchByName(query, pageable)
        return ChannelListResponse(
            channels = channels.content.filter { it.isPublic }.map { channel ->
                ChannelDto(
                    id = channel.id!!,
                    name = channel.name,
                    description = channel.description,
                    avatarUrl = channel.avatarUrl,
                    subscriberCount = channelSubscriberRepository.countByChannelId(channel.id!!).toInt(),
                    isPublic = channel.isPublic,
                    publicId = channel.publicId,
                    inviteLink = channel.inviteLink,
                    owner = channel.owner?.toDto(),
                    isSubscribed = false,
                    isAdmin = false,
                    lastPost = null, // Search results might not need preview
                    unreadCount = 0,
                    createdAt = channel.createdAt
                )
            },
            totalCount = channels.totalElements.toInt()
        )
    }
    // ═══════════════════════════════════════════════════════════════════════════════
    // 🔔 Subscriber Settings (Mute/Pin/Archive)
    // ═══════════════════════════════════════════════════════════════════════════════
    @Transactional
    fun updateSubscriberSettings(channelId: UUID, userId: UUID, isMuted: Boolean?, isPinned: Boolean?, isArchived: Boolean?): ChannelDto? {
        val subscription = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId) ?: return null
        if (isMuted != null) subscription.isMuted = isMuted
        if (isPinned != null) subscription.isPinned = isPinned
        if (isArchived != null) subscription.isArchived = isArchived
        channelSubscriberRepository.save(subscription)
        val channel = channelRepository.findById(channelId).orElse(null) ?: return null
        return channelToDto(channel, userId)
    }

    private fun channelToDto(channel: Channel, userId: UUID): ChannelDto {
        val subscription = channelSubscriberRepository.findByChannelIdAndUserId(channel.id!!, userId)
        val lastPost = channelPostRepository.findTopByChannelIdOrderByCreatedAtDesc(channel.id!!)
        return ChannelDto(
            id = channel.id!!,
            name = channel.name,
            description = channel.description,
            avatarUrl = channel.avatarUrl,
            subscriberCount = channelSubscriberRepository.countByChannelId(channel.id!!).toInt(),
            isPublic = channel.isPublic,
            publicId = channel.publicId,
            inviteLink = channel.inviteLink,
            owner = channel.owner?.toDto(),
            isSubscribed = subscription != null,
            isAdmin = (subscription?.isAdmin == true) || (channel.owner?.id == userId),
            lastPost = lastPost?.toDto(userId),
            unreadCount = 0, // TODO: Implement unread count
            createdAt = channel.createdAt,
            isMuted = subscription?.isMuted ?: false,
            isPinned = subscription?.isPinned ?: false,
            isArchived = subscription?.isArchived ?: false
        )
    }

    @Transactional
    fun addMembers(channelId: UUID, userId: UUID, memberIds: List<UUID>): Boolean {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return false
        // Only OWNER (and maybe Admin) can add members
        if (channel.owner?.id != userId) {
             val subscription = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
             if (subscription == null || !subscription.isAdmin) return false
        }
        
        // Filter out members who are already in
        val newMemberIds = memberIds.filter { 
            !channelSubscriberRepository.existsByChannelIdAndUserId(channelId, it) 
        }
        
        newMemberIds.forEach { memberId ->
            val user = userRepository.findById(memberId).orElse(null)
            if (user != null) {
                val subscriber = ChannelSubscriber().apply {
                    this.channel = channel
                    this.user = user
                    this.isAdmin = false
                    this.subscribedAt = Instant.now()
                }
                channelSubscriberRepository.save(subscriber)
            }
        }
        return true
    }

    @Transactional
    fun deletePost(channelId: UUID, userId: UUID, postId: UUID, deleteForEveryone: Boolean): Boolean {
        val post = channelPostRepository.findById(postId).orElse(null) ?: return false
        val channel = post.channel ?: return false
        
        // Check permissions: Owner or Admin
        if (channel.owner?.id != userId) {
            val subscriber = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
            if (subscriber == null || !subscriber.isAdmin) return false
        }
        
        if (deleteForEveryone) {
            channelPostRepository.delete(post)
        } else {
             // Same logic as messages: Hard delete for now as partial delete not supported
            channelPostRepository.delete(post)
        }
        return true
    }

    @Transactional
    fun reactToPost(channelId: UUID, userId: UUID, postId: UUID, reaction: String?): Boolean {
        val post = channelPostRepository.findById(postId).orElse(null) ?: return false
        if (post.channel?.id != channelId) return false
        
        // Verify user is subscriber? Usually yes, or public channel might allow?
        // Let's assume must be subscriber or owner
        val channel = post.channel!!
        if (!channel.isPublic && channel.owner?.id != userId) {
             if (!channelSubscriberRepository.existsByChannelIdAndUserId(channelId, userId)) return false
        }

        val existingReaction = channelPostReactionRepository.findByPostIdAndUserId(postId, userId)
        if (reaction == null) {
            if (existingReaction != null) {
                channelPostReactionRepository.delete(existingReaction)
            }
        } else {
            if (existingReaction != null) {
                existingReaction.reaction = reaction
                channelPostReactionRepository.save(existingReaction)
            } else {
                val user = userRepository.findById(userId).orElse(null) ?: return false
                val newReaction = ChannelPostReaction().apply {
                    this.post = post
                    this.user = user
                    this.reaction = reaction
                    this.createdAt = Instant.now()
                }
                channelPostReactionRepository.save(newReaction)
            }
        }
        
        // Notify subscribers about update?
        // Optimally, broadcast an update event. For now, simple return true.
        return true
    }

    @Transactional
    fun addComment(channelId: UUID, userId: UUID, postId: UUID, content: String): ChannelPostCommentDto? {
        val post = channelPostRepository.findById(postId).orElse(null) ?: return null
        if (post.channel?.id != channelId) return null
        if (!post.commentsEnabled) return null
        
        // Check if user has access (subscriber or public)
        val channel = post.channel!!
        if (!channel.isPublic && channel.owner?.id != userId) {
            if (!channelSubscriberRepository.existsByChannelIdAndUserId(channelId, userId)) return null
        }
        
        val user = userRepository.findById(userId).orElse(null) ?: return null
        
        val comment = ChannelPostComment().apply {
            this.post = post
            this.user = user
            this.content = content
            this.createdAt = Instant.now()
        }
        val savedComment = channelPostCommentRepository.save(comment)
        return savedComment.toDto()
    }

    fun getComments(channelId: UUID, userId: UUID, postId: UUID, page: Int, size: Int): CommentListResponse {
        val post = channelPostRepository.findById(postId).orElse(null) 
            ?: throw IllegalArgumentException("پست یافت نشد")
        
        // Simple access check
        if (post.channel?.id != channelId) throw IllegalArgumentException("پست نامعتبر")
        
         val pageable = PageRequest.of(page, size)
         val comments = channelPostCommentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable)
         
         return CommentListResponse(
             comments = comments.content.map { it.toDto() },
             totalCount = comments.totalElements.toInt(),
             hasMore = comments.hasNext()
         )
    }


    @Transactional
    fun addAdmin(channelId: UUID, userId: UUID, targetUserId: UUID): Boolean {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return false
        // Only OWNER can add admins
        if (channel.owner?.id != userId) return false
        
        val targetSubscription = channelSubscriberRepository.findByChannelIdAndUserId(channelId, targetUserId)
            ?: return false // Must be a subscriber first
            
        targetSubscription.isAdmin = true
        targetSubscription.isAdmin = true
        channelSubscriberRepository.save(targetSubscription)
        
        // Broadcast admin added
        val subscriberIds = channelSubscriberRepository.findByChannelId(channelId).mapNotNull { it.user?.id }
        val event = com.iliyadev.springboot.websocket.WsChannelSubscriberEvent(
            event = "ADMIN_ADDED",
            channelId = channelId,
            userId = targetUserId,
            isAdmin = true
        )
        webSocketMessageHandler.broadcastChannelSubscriberUpdate(channelId, event, subscriberIds)
        
        return true
    }

    @Transactional
    fun removeAdmin(channelId: UUID, userId: UUID, targetUserId: UUID): Boolean {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return false
        // Only OWNER can remove admins
        if (channel.owner?.id != userId) return false
        
        val targetSubscription = channelSubscriberRepository.findByChannelIdAndUserId(channelId, targetUserId)
            ?: return false
            
        targetSubscription.isAdmin = false
        targetSubscription.isAdmin = false
        channelSubscriberRepository.save(targetSubscription)
        
        // Broadcast admin removed
        val subscriberIds = channelSubscriberRepository.findByChannelId(channelId).mapNotNull { it.user?.id }
        val event = com.iliyadev.springboot.websocket.WsChannelSubscriberEvent(
            event = "ADMIN_REMOVED",
            channelId = channelId,
            userId = targetUserId,
            isAdmin = false
        )
        webSocketMessageHandler.broadcastChannelSubscriberUpdate(channelId, event, subscriberIds)
        
        return true
    }

    @Transactional
    fun toggleInviteLink(channelId: UUID, userId: UUID, enabled: Boolean): InviteLinkResponse? {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return null
        // Check permissions (Owner/Admin)
        if (channel.owner?.id != userId) {
            val sub = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
            if (sub == null || !sub.isAdmin) return null
        }
        
        channel.inviteLink = if (enabled && channel.inviteLink == null) generateInviteLink() else channel.inviteLink
        // channels table needs isInviteLinkEnabled if we want to toggle it without nulling the link? 
        // The Entity has `inviteLink` nullable, but no `isInviteLinkEnabled` boolean like Group.
        // Let's assume nullable link implies enabled/disabled for now or check Entity again.
        // Entity: var inviteLink: String? = null. No boolean flag.
        // If disabled, we set it to null? Or do we need to add the field?
        // Requirement: "Invite link can be regenerated / revoked".
        // If I null it, I lose the code. But "Revoked" implies it's gone.
        // Let's follow Group pattern if possible, but Channel Entity lacks the boolean.
        // I will add the boolean to Channel Entity in a later step if needed, but for now strict nulling is safer for "Revoked".
        
        if (enabled && channel.inviteLink == null) {
            channel.inviteLink = generateInviteLink()
        } else if (!enabled) {
            channel.inviteLink = null
        }
        
        val savedChannel = channelRepository.save(channel)
        return InviteLinkResponse(savedChannel.inviteLink, savedChannel.inviteLink != null)
    }

    @Transactional
    fun regenerateInviteLink(channelId: UUID, userId: UUID): InviteLinkResponse? {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return null
        // Check permissions (Owner/Admin)
        if (channel.owner?.id != userId) {
            val sub = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
            if (sub == null || !sub.isAdmin) return null
        }
        
        channel.inviteLink = generateInviteLink()
        val savedChannel = channelRepository.save(channel)
        return InviteLinkResponse(savedChannel.inviteLink, true)
    }

    @Transactional
    fun joinByInviteLink(inviteCode: String, userId: UUID): ChannelDto? {
        val inviteLink = "https://msgapp.com/c/$inviteCode"
        val channel = channelRepository.findByInviteLink(inviteLink) ?: return null
        
        // Check if already subscribed
        if (channelSubscriberRepository.existsByChannelIdAndUserId(channel.id!!, userId)) {
            return channelToDto(channel, userId)
        }
        
        val user = userRepository.findById(userId).orElse(null) ?: return null
        val subscription = ChannelSubscriber().apply {
            this.channel = channel
            this.user = user
            isAdmin = false
            subscribedAt = Instant.now()
        }
        channelSubscriberRepository.save(subscription)
        return channelToDto(channel, userId)
    }

    fun getChannelSubscribers(channelId: UUID, userId: UUID): List<ChannelSubscriberDto>? {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return null
        // Check permissions (Owner/Admin)
        if (channel.owner?.id != userId) {
            val sub = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
            if (sub == null || !sub.isAdmin) return null
        }
        
        // We need a DTO for subscribers. Existing GroupMemberDto uses MemberRole.
        // I will create a simple inline mapping or need a new DTO.
        // Returning DTOs directly
        return channelSubscriberRepository.findByChannelId(channelId).map { sub ->
            ChannelSubscriberDto(
                user = sub.user!!.toDto(),
                isAdmin = sub.isAdmin,
                joinedAt = sub.subscribedAt
            )
        }
    }

    private fun generateInviteLink(): String = "https://msgapp.com/c/${UUID.randomUUID().toString().take(8)}"
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📁 File Upload Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class FileUploadService {
    private val uploadDir = "uploads"
    fun uploadFile(fileBytes: ByteArray, fileName: String, contentType: String): String {
        val dir = java.io.File(uploadDir)
        if (!dir.exists()) dir.mkdirs()
        val uniqueFileName = "${UUID.randomUUID()}_$fileName"
        val file = java.io.File(dir, uniqueFileName)
        file.writeBytes(fileBytes)
        return "/uploads/$uniqueFileName"
    }
    fun deleteFile(filePath: String): Boolean {
        val file = java.io.File(filePath.removePrefix("/"))
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}
