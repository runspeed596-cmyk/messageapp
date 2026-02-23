package com.Kelasor.app.domain.mapper

import com.Kelasor.app.data.local.entity.*
import com.Kelasor.app.data.remote.dto.*
import com.Kelasor.app.domain.model.*
import java.time.Instant

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 User Mappers
// ═══════════════════════════════════════════════════════════════════════════════

fun UserDto.toDomain(): User {
    // 🔒 DEFENSE IN DEPTH: Client-Side Sanitization 🔒
    // Even though backend filters data, we DOUBLE CHECK here.
    // If backend sent a phone number but privacy settings say we shouldn't see it, we HIDE it.
    
    val profileVis = profileVisibility ?: "EVERYONE"
    val onlineVis = onlineVisibility ?: "EVERYONE"
    val phoneVis = phoneVisibility ?: "CONTACTS"
    
    val isUserContact = isContact ?: false // Default to false for safety
    
    // Strict Privacy Logic:
    val shouldShowPhone = when (phoneVis) {
        "EVERYONE" -> true
        "CONTACTS" -> isUserContact // Only show if isContact is explicitly true
        "NOBODY" -> false
        else -> false
    }
    
    // Enforce Sanitization:
    // If backend sent phone (bug) or cache has phone, but rules say NO -> FORCE HIDE.
    val finalPhoneNumber = if (shouldShowPhone) (phoneNumber ?: "") else ""
    val displayPhone = if (shouldShowPhone && !finalPhoneNumber.isNullOrEmpty()) finalPhoneNumber else "مخفی"

    return User(
        id = id,
        username = username,
        displayName = displayName,
        phoneNumber = finalPhoneNumber, // Sanitized in domain model
        avatarUrl = avatarUrl,
        bio = bio,
        isOnline = isOnline ?: false,
        lastSeen = lastSeen?.let { parseInstant(it) },
        contactName = null,
        
        university = university,
        fieldOfStudy = fieldOfStudy,
        education = education,
        skills = skills,
        interests = interests,
        workExperience = workExperience,
        achievements = achievements,
        isTeacher = isTeacher,
        teachingField = teachingField,
        teachingUniversity = teachingUniversity,
        province = province,
        city = city,
        
        bioChannelId1 = bioChannelId1,
        bioChannelId2 = bioChannelId2,
        
        createdAt = Instant.now(),
        profileVisibility = profileVis,
        onlineVisibility = onlineVis,
        phoneVisibility = phoneVis,
        displayAvatarUrl = avatarUrl,
        displayOnlineStatus = isOnline ?: false,
        displayPhoneNumber = displayPhone
    )
}

fun UserDto.toEntity(isCurrentUser: Boolean = false, contactName: String? = null): UserEntity = UserEntity(
    id = id,
    username = username,
    displayName = displayName,
    phoneNumber = phoneNumber ?: "",
    avatarUrl = avatarUrl,
    bio = bio,
    isOnline = isOnline ?: false,
    lastSeenAt = lastSeen?.let { parseInstant(it)?.toEpochMilli() },
    contactName = contactName,
    isCurrentUser = isCurrentUser,
    isContact = isContact ?: false, // Map security field
    
    university = university,
    fieldOfStudy = fieldOfStudy,
    education = education,
    skills = skills,
    interests = interests,
    workExperience = workExperience,
    achievements = achievements,
    isTeacher = isTeacher,
    teachingField = teachingField,
    teachingUniversity = teachingUniversity,
    province = province,
    city = city,
    
    bioChannelId1 = bioChannelId1,
    bioChannelId2 = bioChannelId2,
    
    profileVisibility = profileVisibility ?: "EVERYONE",
    onlineVisibility = onlineVisibility ?: "EVERYONE",
    phoneVisibility = phoneVisibility ?: "CONTACTS"
)

fun UserEntity.toDomain(): User {
    // 🔒 DEFENSE IN DEPTH: Cache Sanitization 🔒
    // We treat the Local DB as POTENTIALLY STALE/UNTRUSTED for privacy.
    // We re-evaluate privacy rules before displaying data from cache.
    
    // Strict Privacy Logic on Cached Data:
    val shouldShowPhone = when (phoneVisibility) {
        "EVERYONE" -> true
        "CONTACTS" -> isContact // Uses cached relationship status
        "NOBODY" -> false
        else -> false
    }
    
    // Force Hide if rules passed (e.g. user was removed from contacts but cache persists)
    val finalPhoneNumber = if (shouldShowPhone) phoneNumber else ""
    val displayPhone = if (shouldShowPhone && phoneNumber.isNotEmpty()) phoneNumber else "مخفی"

    return User(
        id = id,
        username = username,
        displayName = displayName,
        phoneNumber = finalPhoneNumber, // Sanitized
        avatarUrl = avatarUrl,
        bio = bio,
        isOnline = isOnline,
        lastSeen = lastSeenAt?.let { Instant.ofEpochMilli(it) },
        contactName = contactName,
        
        university = university,
        fieldOfStudy = fieldOfStudy,
        education = education,
        skills = skills,
        interests = interests,
        workExperience = workExperience,
        achievements = achievements,
        isTeacher = isTeacher,
        teachingField = teachingField,
        teachingUniversity = teachingUniversity,
        province = province,
        city = city,
        
        bioChannelId1 = bioChannelId1,
        bioChannelId2 = bioChannelId2,
        
        createdAt = Instant.now(),
        profileVisibility = profileVisibility,
        onlineVisibility = onlineVisibility,
        phoneVisibility = phoneVisibility,
        displayAvatarUrl = avatarUrl,
        displayOnlineStatus = isOnline,
        displayPhoneNumber = displayPhone
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat Mappers
// ═══════════════════════════════════════════════════════════════════════════════

fun ChatDto.toDomain(): Chat = Chat(
    id = id,
    type = ChatType.valueOf(type),
    title = title,
    avatarUrl = avatarUrl,
    lastMessage = lastMessage?.toDomain(),
    unreadCount = unreadCount,
    isPinned = isPinned,
    isMuted = isMuted,
    isArchived = isArchived,
    participants = participants.map { it.toDomain() },
    updatedAt = parseInstant(updatedAt) ?: Instant.now()
)

fun ChatDto.toEntity(): ChatEntity = ChatEntity(
    id = id,
    type = type,
    title = title,
    avatarUrl = avatarUrl,
    lastMessageId = lastMessage?.id,
    lastMessage = lastMessage?.content,
    lastMessageTime = lastMessage?.createdAt?.let { parseInstant(it)?.toEpochMilli() },
    isLastMessageEdited = lastMessage?.isEdited ?: false,
    unreadCount = unreadCount,
    isPinned = isPinned,
    isMuted = isMuted,
    isArchived = isArchived,
    updatedAt = parseInstant(updatedAt)?.toEpochMilli() ?: System.currentTimeMillis()
)

fun ChatEntity.toDomain(participants: List<User> = emptyList()): Chat {
    // Create a simplified last message from stored preview data
    val lastMsg = if (lastMessage != null && lastMessageTime != null) {
        Message(
            id = lastMessageId ?: "", // Use stored message ID if available
            chatId = id,
            senderId = "",
            senderName = "",
            senderAvatar = null,
            type = MessageType.TEXT,
            content = lastMessage,
            mediaUrl = null,
            replyToMessageId = null,
            replyToMessage = null,
            forwardedFrom = null,
            status = MessageStatus.DELIVERED,
            isEdited = isLastMessageEdited,
            createdAt = Instant.ofEpochMilli(lastMessageTime),
            editedAt = null
        )
    } else null
    
    return Chat(
        id = id,
        type = ChatType.valueOf(type),
        title = title,
        avatarUrl = avatarUrl,
        lastMessage = lastMsg,
        unreadCount = unreadCount,
        isPinned = isPinned,
        isMuted = isMuted,
        isArchived = isArchived,
        participants = participants,
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )
}

fun ChatWithParticipants.toDomain(): Chat {
    return chat.toDomain(participants = participants.map { it.toDomain() })
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Message Mappers
// ═══════════════════════════════════════════════════════════════════════════════

fun MessageDto.toDomain(): Message = Message(
    id = id,
    chatId = chatId,
    senderId = senderId,
    senderName = senderName,
    senderAvatar = senderAvatar,
    type = MessageType.valueOf(type),
    content = content,
    mediaUrl = mediaUrl,
    replyToMessageId = replyToMessageId,
    replyToMessage = replyToMessage?.toDomain(),
    forwardedFrom = forwardedFrom,
    status = try { MessageStatus.valueOf(status) } catch (e: Exception) { MessageStatus.SENT },
    isEdited = isEdited,
    createdAt = parseInstant(createdAt) ?: Instant.now(),
    editedAt = editedAt?.let { parseInstant(it) },
    reactions = reactions,
    myReaction = myReaction,
    poll = poll?.toDomain(),
    amplitudes = amplitudes,
    isPinned = isPinned,
    pinnedAt = pinnedAt?.let { parseInstant(it) },
    scheduledAt = scheduledAt?.let { parseInstant(it) }
)

fun MessageDto.toEntity(): MessageEntity = MessageEntity(
    id = id,
    chatId = chatId,
    senderId = senderId,
    senderName = senderName,
    senderAvatar = senderAvatar,
    type = type,
    content = content,
    mediaUrl = mediaUrl,
    replyToMessageId = replyToMessageId,
    replyToMessage = if (replyToMessage != null) com.google.gson.Gson().toJson(replyToMessage) else null,
    forwardedFrom = forwardedFrom,
    status = status,
    isEdited = isEdited,
    createdAt = parseInstant(createdAt)?.toEpochMilli() ?: System.currentTimeMillis(),
    editedAt = editedAt?.let { parseInstant(it)?.toEpochMilli() },
    isSynced = true,
    reactions = com.google.gson.Gson().toJson(reactions),
    myReaction = myReaction,
    amplitudes = amplitudes?.joinToString(","),
    isPinned = isPinned,
    pinnedAt = pinnedAt?.let { parseInstant(it)?.toEpochMilli() },
    scheduledAt = scheduledAt?.let { parseInstant(it)?.toEpochMilli() }
)

fun MessageEntity.toDomain(): Message = Message(
    id = id,
    chatId = chatId,
    senderId = senderId,
    senderName = senderName,
    senderAvatar = senderAvatar,
    type = try {
        MessageType.valueOf(type)
    } catch (e: Exception) {
        android.util.Log.e("Mappers", "Invalid message type '$type' for msg $id. Defaulting to TEXT.")
        MessageType.TEXT
    },
    content = content,
    mediaUrl = mediaUrl.also { 
        if (type != "TEXT" && it == null) {
            android.util.Log.w("Mappers", "Media message ($type) has NULL mediaUrl! MsgId: $id")
        }
    },
    replyToMessageId = replyToMessageId,
    replyToMessage = if (replyToMessage != null) {
        try {
            // First try to parse as MessageDto (common case from API)
            val dto = com.google.gson.Gson().fromJson(replyToMessage, MessageDto::class.java)
            dto.toDomain()
        } catch (e: Exception) {
            try {
                // Fallback: try parsing as Message domain object (if saved locally that way)
                com.google.gson.Gson().fromJson(replyToMessage, Message::class.java)
            } catch (e2: Exception) { null }
        }
    } else null,
    forwardedFrom = forwardedFrom,
    status = MessageStatus.valueOf(status),
    isEdited = isEdited,
    createdAt = Instant.ofEpochMilli(createdAt),
    editedAt = editedAt?.let { Instant.ofEpochMilli(it) },
    reactions = if (reactions != null) {
        try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Int>>() {}.type
            com.google.gson.Gson().fromJson(reactions, type)
        } catch (e: Exception) { emptyMap() }
    } else emptyMap(),
    myReaction = myReaction,
    amplitudes = amplitudes?.split(",")?.mapNotNull { it.toIntOrNull() },
    isPinned = isPinned,
    pinnedAt = pinnedAt?.let { Instant.ofEpochMilli(it) },
    scheduledAt = scheduledAt?.let { Instant.ofEpochMilli(it) }
)

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Mappers
// ═══════════════════════════════════════════════════════════════════════════════

fun GroupDto.toDomain(): Group = Group(
    id = id,
    name = name,
    description = description,
    avatarUrl = avatarUrl,
    members = emptyList(),
    memberCount = memberCount,
    isPublic = isPublic,
    inviteLink = inviteLink,
    isInviteLinkEnabled = isInviteLinkEnabled,
    allowMembersToSendMessages = allowMembersToSendMessages,
    allowMembersToEditInfo = allowMembersToEditInfo,
    myRole = myRole?.let { MemberRole.valueOf(it) },
    lastMessage = lastMessage?.toDomain(),

    unreadCount = unreadCount,
    createdAt = parseInstant(createdAt) ?: Instant.now(),
    createdBy = createdBy?.toDomain(),
    isMuted = isMuted,
    isPinned = isPinned,
    isArchived = isArchived
)

fun GroupDto.toEntity(): GroupEntity = GroupEntity(
    id = id,
    name = name,
    description = description,
    avatarUrl = avatarUrl,
    memberCount = memberCount,
    isPublic = isPublic,
    inviteLink = inviteLink,
    isInviteLinkEnabled = isInviteLinkEnabled,
    allowMembersToSendMessages = allowMembersToSendMessages,
    allowMembersToEditInfo = allowMembersToEditInfo,
    myRole = myRole,
    lastMessageContent = lastMessage?.content,
    lastMessageTime = lastMessage?.createdAt?.let { parseInstant(it)?.toEpochMilli() },
    isLastMessageEdited = lastMessage?.isEdited ?: false,
    unreadCount = unreadCount,

    createdAt = parseInstant(createdAt)?.toEpochMilli() ?: System.currentTimeMillis(),
    isMuted = isMuted,
    isPinned = isPinned,
    isArchived = isArchived
)

fun GroupEntity.toDomain(): Group {
    val lastMsg = if (lastMessageContent != null && lastMessageTime != null) {
        Message(
            id = "", 
            chatId = id,
            senderId = "",
            senderName = "",
            senderAvatar = null,
            type = MessageType.TEXT,
            content = lastMessageContent,
            mediaUrl = null,
            replyToMessageId = null,
            replyToMessage = null,
            forwardedFrom = null,
            status = MessageStatus.DELIVERED,
            isEdited = isLastMessageEdited,
            createdAt = Instant.ofEpochMilli(lastMessageTime),
            editedAt = null
        )
    } else null

    return Group(
        id = id,
        name = name,
        description = description,
        avatarUrl = avatarUrl,
        members = emptyList(),
        memberCount = memberCount,
        isPublic = isPublic,
        inviteLink = inviteLink,
        isInviteLinkEnabled = isInviteLinkEnabled,
        allowMembersToSendMessages = allowMembersToSendMessages,
        allowMembersToEditInfo = allowMembersToEditInfo,
        myRole = myRole?.let { MemberRole.valueOf(it) },
        lastMessage = lastMsg,
        unreadCount = unreadCount,
        createdAt = Instant.ofEpochMilli(createdAt),
        createdBy = null,
        isArchived = isArchived,
        isPinned = isPinned,
        isMuted = isMuted
    )
}

fun GroupMemberDto.toEntity(groupId: String): GroupMemberEntity = GroupMemberEntity(
    id = "${groupId}_${user.id}",
    groupId = groupId,
    userId = user.id,
    role = role,
    joinedAt = parseInstant(joinedAt)?.toEpochMilli() ?: System.currentTimeMillis(),
    canEditInfo = canEditInfo,
    canPostStory = canPostStory,
    canAddMembers = canAddMembers,
    canRemoveMembers = canRemoveMembers
)

fun GroupMessageDto.toDomain(): Message = Message(
    id = id ?: "",
    chatId = groupId ?: "",
    senderId = senderId ?: "",
    senderName = senderName ?: "",
    senderAvatar = senderAvatar,
    type = try { MessageType.valueOf(type ?: "TEXT") } catch (e: Exception) { MessageType.TEXT },
    content = content ?: "",
    mediaUrl = mediaUrl,
    replyToMessageId = replyToMessageId,
    replyToMessage = replyToMessage?.toDomain(),
    forwardedFrom = forwardedFrom,
    status = try { MessageStatus.valueOf(status ?: "SENT") } catch (e: Exception) { MessageStatus.SENT },
    isEdited = isEdited,
    createdAt = parseInstant(createdAt) ?: Instant.now(),
    editedAt = editedAt?.let { parseInstant(it) },
    reactions = reactions,
    myReaction = myReaction,
    poll = poll?.toDomain(),
    amplitudes = amplitudes,
    isPinned = isPinned,
    pinnedAt = pinnedAt?.let { parseInstant(it) },
    scheduledAt = scheduledAt?.let { parseInstant(it) }
)

fun GroupMessageDto.toEntity(): GroupMessageEntity = GroupMessageEntity(
    id = id ?: "",
    groupId = groupId ?: "",
    senderId = senderId ?: "",
    senderName = senderName ?: "",
    senderAvatar = senderAvatar,
    type = type ?: "TEXT",
    content = content ?: "",
    mediaUrl = mediaUrl,
    replyToMessageId = replyToMessageId,
    replyToMessage = if (replyToMessage != null) com.google.gson.Gson().toJson(replyToMessage) else null,
    isEdited = isEdited,
    createdAt = parseInstant(createdAt)?.toEpochMilli() ?: System.currentTimeMillis(),
    editedAt = editedAt?.let { parseInstant(it)?.toEpochMilli() },
    isSynced = true,
    reactions = com.google.gson.Gson().toJson(reactions),
    myReaction = myReaction,
    poll = if (poll != null) com.google.gson.Gson().toJson(poll) else null,
    amplitudes = amplitudes?.joinToString(",")
)

fun GroupMessageEntity.toDomain(replyMessage: Message? = null): Message = Message(
    id = id,
    chatId = groupId,
    senderId = senderId,
    senderName = senderName,
    senderAvatar = senderAvatar,
    type = MessageType.valueOf(type),
    content = content,
    mediaUrl = mediaUrl,
    replyToMessageId = replyToMessageId,
    replyToMessage = replyMessage ?: if (replyToMessage != null) {
        try {
            com.google.gson.Gson().fromJson(replyToMessage, Message::class.java)
        } catch (e: Exception) { null }
    } else null,
    forwardedFrom = null,
    status = if (isSynced) MessageStatus.DELIVERED else MessageStatus.PENDING,
    isEdited = isEdited,
    createdAt = Instant.ofEpochMilli(createdAt),
    editedAt = editedAt?.let { Instant.ofEpochMilli(it) },
    reactions = if (reactions != null) {
        try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Int>>() {}.type
            com.google.gson.Gson().fromJson(reactions, type)
        } catch (e: Exception) { emptyMap() }
    } else emptyMap(),
    myReaction = myReaction,
    poll = if (poll != null) {
        try {
            com.google.gson.Gson().fromJson(poll, com.Kelasor.app.data.remote.dto.PollDto::class.java)?.toDomain()
        } catch (e: Exception) { null }
    } else null,
    amplitudes = amplitudes?.split(",")?.mapNotNull { it.toIntOrNull() }
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Mappers
// ═══════════════════════════════════════════════════════════════════════════════

fun ChannelDto.toDomain(): Channel = Channel(
    id = id,
    name = name,
    description = description,
    avatarUrl = avatarUrl,
    subscriberCount = subscriberCount,
    isPublic = isPublic,
    publicId = publicId,
    inviteLink = inviteLink,
    owner = owner?.toDomain(),
    isSubscribed = isSubscribed,
    isAdmin = isAdmin,
    lastPost = lastPost?.toDomain(),
    unreadCount = unreadCount,

    createdAt = parseInstant(createdAt) ?: Instant.now(),
    isMuted = isMuted,
    isPinned = isPinned,
    isArchived = isArchived
)

fun ChannelDto.toEntity(): ChannelEntity = ChannelEntity(
    id = id,
    name = name,
    description = description,
    avatarUrl = avatarUrl,
    subscriberCount = subscriberCount,
    isPublic = isPublic,
    publicId = publicId,
    inviteLink = inviteLink,
    ownerId = owner?.id,
    isSubscribed = isSubscribed,
    isAdmin = isAdmin,
    lastPostContent = lastPost?.content,
    lastPostTime = lastPost?.createdAt?.let { parseInstant(it)?.toEpochMilli() },
    isLastPostEdited = lastPost?.isEdited ?: false,
    unreadCount = unreadCount,

    createdAt = parseInstant(createdAt)?.toEpochMilli() ?: System.currentTimeMillis(),
    isMuted = isMuted,
    isPinned = isPinned,
    isArchived = isArchived
)

fun ChannelEntity.toDomain(): Channel {
    val lastPost = if (lastPostContent != null && lastPostTime != null) {
        ChannelPost(
            id = "", // Placeholder
            channelId = id,
            type = MessageType.TEXT, // Default for preview
            content = lastPostContent,
            mediaUrl = null,
            viewCount = 0,
            reactions = emptyMap(),
            commentsEnabled = true,
            commentCount = 0,
            createdAt = Instant.ofEpochMilli(lastPostTime),
            editedAt = null,
            isEdited = isLastPostEdited
        )
    } else null
    
    // Create a minimal User from ownerId for ownership comparison
    val ownerUser = ownerId?.let { id ->
        User(
            id = id,
            username = "",
            displayName = "",
            phoneNumber = "",
            avatarUrl = null,
            bio = null,
            isOnline = false,
            lastSeen = null,
            createdAt = Instant.now()
        )
    }

    return Channel(
        id = id,
        name = name,
        description = description,
        avatarUrl = avatarUrl,
        subscriberCount = subscriberCount,
        isPublic = isPublic,
        publicId = publicId,
        inviteLink = inviteLink,
        owner = ownerUser,
        isSubscribed = isSubscribed,
        isAdmin = isAdmin,
        lastPost = lastPost,
        unreadCount = unreadCount,
        createdAt = Instant.ofEpochMilli(createdAt),
        isArchived = isArchived,
        isPinned = isPinned,
        isMuted = isMuted
    )
}

fun ChannelPostDto.toDomain(): ChannelPost = ChannelPost(
    id = id ?: "",
    channelId = channelId ?: "",
    type = try { MessageType.valueOf(type ?: "TEXT") } catch (e: Exception) { MessageType.TEXT },
    content = content ?: "",
    mediaUrl = mediaUrl,
    viewCount = viewCount,
    reactions = reactions,
    commentsEnabled = commentsEnabled,
    commentCount = commentCount,
    createdAt = parseInstant(createdAt) ?: Instant.now(),
    editedAt = editedAt?.let { parseInstant(it) },
    poll = poll?.toDomain(),
    amplitudes = amplitudes,
    myReaction = myReaction,
    isPinned = isPinned,
    pinnedAt = pinnedAt?.let { parseInstant(it) },
    scheduledAt = scheduledAt?.let { parseInstant(it) },
    forwardedFrom = forwardedFrom,
    isEdited = isEdited
)

fun ChannelPostDto.toEntity(): ChannelPostEntity = ChannelPostEntity(
    id = id ?: "",
    channelId = channelId ?: "",
    type = type ?: "TEXT",
    content = content ?: "",
    mediaUrl = mediaUrl,
    viewCount = viewCount,
    commentsEnabled = commentsEnabled,
    createdAt = parseInstant(createdAt)?.toEpochMilli() ?: System.currentTimeMillis(),
    editedAt = editedAt?.let { parseInstant(it)?.toEpochMilli() },
    poll = if (poll != null) com.google.gson.Gson().toJson(poll) else null,
    reactions = if (reactions.isNotEmpty()) com.google.gson.Gson().toJson(reactions) else null,
    amplitudes = amplitudes?.joinToString(","),
    isPinned = isPinned,
    pinnedAt = pinnedAt?.let { parseInstant(it)?.toEpochMilli() },
    scheduledAt = scheduledAt?.let { parseInstant(it)?.toEpochMilli() },
    forwardedFrom = forwardedFrom,
    isEdited = isEdited,
    myReaction = myReaction
)

fun ChannelPostEntity.toDomain(): ChannelPost = ChannelPost(
    id = id,
    channelId = channelId,
    type = MessageType.valueOf(type),
    content = content,
    mediaUrl = mediaUrl,
    viewCount = viewCount,
    reactions = if (reactions != null) {
        try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Int>>() {}.type
            com.google.gson.Gson().fromJson(reactions, type)
        } catch (e: Exception) { emptyMap() }
    } else emptyMap(),
    commentsEnabled = commentsEnabled,
    commentCount = 0,
    createdAt = Instant.ofEpochMilli(createdAt),
    editedAt = editedAt?.let { Instant.ofEpochMilli(it) },
    poll = if (poll != null) {
        try {
             com.google.gson.Gson().fromJson(poll, com.Kelasor.app.data.remote.dto.PollDto::class.java)?.toDomain()
        } catch (e: Exception) { null }
    } else null,
    amplitudes = amplitudes?.split(",")?.mapNotNull { it.toIntOrNull() },
    myReaction = myReaction,
    isPinned = isPinned,
    pinnedAt = pinnedAt?.let { Instant.ofEpochMilli(it) },
    scheduledAt = scheduledAt?.let { Instant.ofEpochMilli(it) },
    forwardedFrom = forwardedFrom,
    isEdited = isEdited
)

fun ChannelSubscriberDto.toDomain(): ChannelSubscriber = ChannelSubscriber(
    user = user.toDomain(),
    isAdmin = isAdmin,
    joinedAt = parseInstant(joinedAt) ?: Instant.now()
)

fun ChannelSubscriberDto.toEntity(channelId: String): ChannelSubscriberEntity = ChannelSubscriberEntity(
    id = "${channelId}_${user.id}",
    channelId = channelId,
    userId = user.id,
    isAdmin = isAdmin,
    joinedAt = parseInstant(joinedAt)?.toEpochMilli() ?: System.currentTimeMillis()
)

fun ChannelSubscriberEntity.toDomain(user: User): ChannelSubscriber = ChannelSubscriber(
    user = user,
    isAdmin = isAdmin,
    joinedAt = Instant.ofEpochMilli(joinedAt)
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔧 Helper Functions
// ═══════════════════════════════════════════════════════════════════════════════

internal fun parseInstant(dateString: String?): Instant? {
    if (dateString == null) return null
    return try {
        Instant.parse(dateString)
    } catch (e: Exception) {
        dateString.toLongOrNull()?.let { Instant.ofEpochMilli(it) }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📊 Poll Mappers
// ═══════════════════════════════════════════════════════════════════════════════

fun PollDto.toDomain(): Poll = Poll(
    id = id,
    question = question,
    isMultipleChoice = isMultipleChoice,
    isAnonymous = isAnonymous,
    options = options.map { it.toDomain() },
    totalVotes = totalVotes,
    userVotedOptionIds = userVotedOptionIds,
    createdAt = parseInstant(createdAt) ?: Instant.now()
)

fun PollOptionDto.toDomain(): PollOption = PollOption(
    id = id,
    text = text,
    voteCount = voteCount,
    votePercentage = votePercentage
)
