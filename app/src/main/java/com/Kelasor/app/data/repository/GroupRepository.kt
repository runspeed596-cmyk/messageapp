package com.Kelasor.app.data.repository

import com.Kelasor.app.data.local.dao.*
import com.Kelasor.app.data.local.entity.GroupMessageEntity
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.*
import com.Kelasor.app.domain.mapper.*
import com.Kelasor.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import javax.inject.Inject
import javax.inject.Singleton

sealed class GroupResult<out T> {
    data class Success<T>(val data: T) : GroupResult<T>()
    data class Error(val message: String) : GroupResult<Nothing>()
    data object Loading : GroupResult<Nothing>()
}

@Singleton
class GroupRepository @Inject constructor(
    private val apiService: ApiService,
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val groupMessageDao: GroupMessageDao,
    private val userDao: UserDao,
    private val sessionManager: com.Kelasor.app.data.session.SessionManager
) {
    fun observeGroups(): Flow<List<Group>> = groupDao.observeAllGroups().map { groups ->
        groups.map { it.toDomain() }
    }.distinctUntilChanged()
    fun observeGroup(groupId: String): Flow<Group?> = groupDao.observeGroupById(groupId).map { it?.toDomain() }.distinctUntilChanged()
    suspend fun getGroups(page: Int = 0, forceRefresh: Boolean = false): Flow<GroupResult<List<Group>>> = flow {
        emit(GroupResult.Loading)
        val cachedGroups = groupDao.observeAllGroups().first()
        val archivedGroups = groupDao.observeArchivedGroups().first()
        val allCachedGroups = cachedGroups + archivedGroups
        
        // Create a map of existing local flags (isArchived, isPinned)
        val localFlagsMap = allCachedGroups.associate { it.id to Pair(it.isArchived, it.isPinned) }
        
        if (allCachedGroups.isNotEmpty() && !forceRefresh) {
            emit(GroupResult.Success(allCachedGroups.map { it.toDomain() }))
        }
        try {
            val response = apiService.getGroups(page)
            if (response.isSuccessful) {
                val groupDtos = response.body()?.groups ?: emptyList()
                // Preserve local flags when inserting from API
                val entitiesToInsert = groupDtos.map { dto ->
                    val entity = dto.toEntity()
                    val localFlags = localFlagsMap[entity.id]
                    if (localFlags != null) {
                        entity.copy(isArchived = localFlags.first, isPinned = localFlags.second)
                    } else {
                        entity
                    }
                }
                groupDao.insertGroups(entitiesToInsert)
                emit(GroupResult.Success(groupDtos.map { it.toDomain() }))
            } else {
                if (allCachedGroups.isEmpty()) {
                    emit(GroupResult.Error("خطا در دریافت گروه‌ها"))
                }
            }
        } catch (e: Exception) {
            if (allCachedGroups.isEmpty()) {
                emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
            }
        }
    }
    suspend fun getGroupById(groupId: String): Flow<GroupResult<Group>> = flow {
        emit(GroupResult.Loading)
        val cachedGroup = groupDao.getGroupById(groupId)
        if (cachedGroup != null) {
            emit(GroupResult.Success(cachedGroup.toDomain()))
        }
        try {
            val response = apiService.getGroupById(groupId)
            if (response.isSuccessful && response.body()?.success == true) {
                val groupDto = response.body()?.data
                if (groupDto != null) {
                    // Preserve local isArchived and isPinned flags
                    val newEntity = groupDto.toEntity()
                    val entityToSave = if (cachedGroup != null) {
                        newEntity.copy(isArchived = cachedGroup.isArchived, isPinned = cachedGroup.isPinned)
                    } else {
                        newEntity
                    }
                    groupDao.insertGroup(entityToSave)
                    emit(GroupResult.Success(entityToSave.toDomain()))
                }
            } else {
                if (cachedGroup == null) {
                    emit(GroupResult.Error(response.body()?.message ?: "گروه یافت نشد"))
                }
            }
        } catch (e: Exception) {
            if (cachedGroup == null) {
                emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
            }
        }
    }
    suspend fun createGroup(
        name: String,
        description: String?,
        isPublic: Boolean,
        memberIds: List<String>,
        avatarFile: java.io.File? = null
    ): Flow<GroupResult<Group>> = flow {
        emit(GroupResult.Loading)
        try {
            var avatarUrl: String? = null
            
            // Upload avatar if provided
            if (avatarFile != null) {
                val requestBody = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), avatarFile)
                val part = okhttp3.MultipartBody.Part.createFormData("file", avatarFile.name, requestBody)
                val uploadResponse = apiService.uploadFile(part)
                if (uploadResponse.isSuccessful && uploadResponse.body()?.success == true) {
                    avatarUrl = uploadResponse.body()?.data
                } else {
                    emit(GroupResult.Error("خطا در آپلود تصویر گروه: ${uploadResponse.body()?.message}"))
                    return@flow
                }
            }
            
            val response = apiService.createGroup(CreateGroupRequest(name, description, isPublic, memberIds, avatarUrl))
            if (response.isSuccessful && response.body()?.success == true) {
                val groupDto = response.body()?.data
                if (groupDto != null) {
                    groupDao.insertGroup(groupDto.toEntity())
                    emit(GroupResult.Success(groupDto.toDomain()))
                } else {
                    emit(GroupResult.Error("خطا در ایجاد گروه"))
                }
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در ایجاد گروه"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun updateGroup(
        groupId: String,
        name: String?,
        description: String?,
        isPublic: Boolean?,
        avatarFile: java.io.File? = null
    ): Flow<GroupResult<Group>> = flow {
        emit(GroupResult.Loading)
        try {
            var avatarUrl: String? = null
            
            if (avatarFile != null) {
                val requestBody = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), avatarFile)
                val part = okhttp3.MultipartBody.Part.createFormData("file", avatarFile.name, requestBody)
                val uploadResponse = apiService.uploadFile(part)
                if (uploadResponse.isSuccessful && uploadResponse.body()?.success == true) {
                    avatarUrl = uploadResponse.body()?.data
                } else {
                    emit(GroupResult.Error("خطا در آپلود تصویر گروه: ${uploadResponse.body()?.message}"))
                    return@flow
                }
            }

            val response = apiService.updateGroup(groupId, UpdateGroupRequest(name, description, isPublic, avatarUrl))
            if (response.isSuccessful && response.body()?.success == true) {
                val groupDto = response.body()?.data
                if (groupDto != null) {
                    groupDao.insertGroup(groupDto.toEntity())
                    emit(GroupResult.Success(groupDto.toDomain()))
                }
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در بروزرسانی گروه"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun deleteGroup(groupId: String): Flow<GroupResult<Unit>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.deleteGroup(groupId)
            if (response.isSuccessful && response.body()?.success == true) {
                groupDao.getGroupById(groupId)?.let { groupDao.deleteGroup(it) }
                emit(GroupResult.Success(Unit))
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در حذف گروه"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    fun observeGroupMembers(groupId: String): Flow<List<GroupMember>> =
        groupMemberDao.observeMembersForGroup(groupId).map { members ->
            members.mapNotNull { member ->
                val user = userDao.getUserById(member.userId)
                if (user != null) {
                    GroupMember(
                        user = user.toDomain(),
                        role = MemberRole.valueOf(member.role),
                        joinedAt = java.time.Instant.ofEpochMilli(member.joinedAt),
                        canEditInfo = member.canEditInfo,
                        canPostStory = member.canPostStory,
                        canAddMembers = member.canAddMembers,
                        canRemoveMembers = member.canRemoveMembers
                    )
                } else null
            }
        }.distinctUntilChanged()
    suspend fun getGroupMembers(groupId: String): Flow<GroupResult<List<GroupMember>>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.getGroupMembers(groupId)
            if (response.isSuccessful && response.body()?.success == true) {
                val memberDtos = response.body()?.data ?: emptyList()
                // Cache users and members
                memberDtos.forEach { memberDto ->
                    userDao.insertUser(memberDto.user.toEntity())
                    groupMemberDao.insertMember(memberDto.toEntity(groupId))
                }
                val members = memberDtos.map { memberDto ->
                    GroupMember(
                        user = memberDto.user.toDomain(),
                        role = MemberRole.valueOf(memberDto.role),
                        joinedAt = java.time.Instant.now(),
                        canEditInfo = memberDto.canEditInfo,
                        canPostStory = memberDto.canPostStory,
                        canAddMembers = memberDto.canAddMembers,
                        canRemoveMembers = memberDto.canRemoveMembers
                    )
                }
                emit(GroupResult.Success(members))
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در دریافت اعضا"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun addMembers(groupId: String, memberIds: List<String>): Flow<GroupResult<Unit>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.addGroupMembers(groupId, AddGroupMembersRequest(memberIds))
            if (response.isSuccessful && response.body()?.success == true) {
                // Refresh members from server to update local cache
                getGroupMembers(groupId).collect { } 
                emit(GroupResult.Success(Unit))
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در افزودن اعضا"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun removeMember(groupId: String, memberId: String): Flow<GroupResult<Unit>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.removeGroupMember(groupId, memberId)
            if (response.isSuccessful && response.body()?.success == true) {
                groupMemberDao.removeMember(groupId, memberId)
                
                // If the removed member is the current user, delete the group locally
                val currentUserId = sessionManager.userId.first()
                if (memberId == currentUserId) {
                    groupDao.deleteGroupById(groupId)
                }
                
                emit(GroupResult.Success(Unit))
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در حذف عضو"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    fun observeGroupMessages(groupId: String): Flow<List<Message>> =
        groupMessageDao.observeMessagesForGroup(groupId).map { messages ->
            messages.map { it.message.toDomain(replyMessage = it.replyToMessage?.toDomain()) }
        }.distinctUntilChanged()
    suspend fun getGroupMessages(
        groupId: String,
        page: Int = 0
    ): Flow<GroupResult<List<Message>>> = flow {
        emit(GroupResult.Loading)
        val cachedMessages = groupMessageDao.getMessagesForGroup(groupId, 50, page * 50)
        if (cachedMessages.isNotEmpty()) {
            emit(GroupResult.Success(cachedMessages.map { it.toDomain() }))
        }
        try {
            val response = apiService.getGroupMessages(groupId, page)
            if (response.isSuccessful) {
                val messageDtos = response.body()?.messages ?: emptyList()
                messageDtos.forEach {
                    groupMessageDao.insertMessage(it.toEntity())
                }
                emit(GroupResult.Success(messageDtos.map { it.toDomain() }))
            }
        } catch (e: Exception) {
            if (cachedMessages.isEmpty()) {
                emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
            }
        }
    }
    suspend fun sendGroupMessage(
        groupId: String,
        content: String,
        type: String = "TEXT",
        mediaUrl: String? = null,
        replyToMessageId: String? = null,
        amplitudes: List<Int>? = null
    ): Flow<GroupResult<Message>> = flow {
        emit(GroupResult.Loading)
        val localMessage = GroupMessageEntity(
            id = "local_${System.currentTimeMillis()}",
            groupId = groupId,
            senderId = "",
            senderName = "",
            senderAvatar = null,
            type = type,
            content = content,
            mediaUrl = mediaUrl,
            replyToMessageId = replyToMessageId,
            isEdited = false,
            createdAt = System.currentTimeMillis(),
            editedAt = null,
            isSynced = false,
            amplitudes = amplitudes?.joinToString(",")
        )
        groupMessageDao.insertMessage(localMessage)
        try {
            val response = apiService.sendGroupMessage(
                groupId,
                SendGroupMessageRequest(type, content, mediaUrl, replyToMessageId, null, amplitudes)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val messageDto = response.body()?.data
                if (messageDto != null) {
                    groupMessageDao.deleteMessage(localMessage)
                    // CRITICAL FIX: ALWAYS patch type, mediaUrl, and amplitudes from local message
                    // Backend may return incorrect type (e.g., TEXT instead of AUDIO/VIDEO)
                    val finalDto = messageDto.copy(
                        type = localMessage.type,
                        mediaUrl = localMessage.mediaUrl ?: messageDto.mediaUrl,
                        amplitudes = localMessage.amplitudes?.split(",")?.mapNotNull { it.toIntOrNull() } ?: messageDto.amplitudes
                    )
                    groupMessageDao.insertMessage(finalDto.toEntity())
                    emit(GroupResult.Success(finalDto.toDomain()))
                } else {
                    emit(GroupResult.Error("خطا در ارسال پیام"))
                }
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در ارسال پیام"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun updateGroupSettings(
        groupId: String,
        allowMembersToSendMessages: Boolean?,
        allowMembersToEditInfo: Boolean?
    ): Flow<GroupResult<Group>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.updateGroupSettings(
                groupId,
                UpdateGroupSettingsRequest(allowMembersToSendMessages, allowMembersToEditInfo)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val groupDto = response.body()?.data
                if (groupDto != null) {
                    groupDao.insertGroup(groupDto.toEntity())
                    emit(GroupResult.Success(groupDto.toDomain()))
                }
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در بروزرسانی تنظیمات"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun toggleInviteLink(groupId: String, enabled: Boolean): Flow<GroupResult<InviteLinkResponse>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.toggleGroupInviteLink(groupId, enabled)
            if (response.isSuccessful && response.body()?.success == true) {
                val result = response.body()?.data
                if (result != null) {
                    emit(GroupResult.Success(result))
                }
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در تغییر لینک دعوت"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun regenerateInviteLink(groupId: String): Flow<GroupResult<InviteLinkResponse>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.regenerateGroupInviteLink(groupId)
            if (response.isSuccessful && response.body()?.success == true) {
                val result = response.body()?.data
                if (result != null) {
                    emit(GroupResult.Success(result))
                }
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در ایجاد لینک جدید"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun joinByInviteLink(inviteCode: String): Flow<GroupResult<Group>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.joinGroupByInviteLink(inviteCode)
            if (response.isSuccessful && response.body()?.success == true) {
                val groupDto = response.body()?.data
                if (groupDto != null) {
                    groupDao.insertGroup(groupDto.toEntity())
                    emit(GroupResult.Success(groupDto.toDomain()))
                }
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "لینک نامعتبر یا غیرفعال است"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun changeRole(
        groupId: String, 
        memberId: String, 
        role: MemberRole,
        canEditInfo: Boolean = false,
        canPostStory: Boolean = false,
        canAddMembers: Boolean = false,
        canRemoveMembers: Boolean = false
    ): Flow<GroupResult<Unit>> = flow {
        emit(GroupResult.Loading)
        try {
            val request = ChangeRoleRequest(
                role.name,
                canEditInfo,
                canPostStory,
                canAddMembers,
                canRemoveMembers
            )
            val response = apiService.changeGroupMemberRole(groupId, memberId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                groupMemberDao.updateMemberRole(groupId, memberId, role.name)
                emit(GroupResult.Success(Unit))
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در تغییر نقش"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun reactToMessage(groupId: String, messageId: String, reaction: String): Flow<GroupResult<Unit>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.reactToGroupMessage(groupId, messageId, ReactionRequest(reaction))
            if (response.isSuccessful && response.body()?.success == true) {
                // Update local database with the reaction
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<Map<String, Int>>() {}.type
                
                val msg = groupMessageDao.getMessageById(messageId)
                if (msg != null) {
                    val currentReactions: MutableMap<String, Int> = try {
                        gson.fromJson(msg.reactions, type) ?: mutableMapOf()
                    } catch (e: Exception) { mutableMapOf() }
                    
                    // Update counts based on old and new reactions
                    val oldReaction = msg.myReaction
                    if (oldReaction != null && oldReaction != reaction) {
                        val count = currentReactions[oldReaction] ?: 0
                        if (count > 1) currentReactions[oldReaction] = count - 1 else currentReactions.remove(oldReaction)
                    }
                    if (oldReaction != reaction) {
                        currentReactions[reaction] = (currentReactions[reaction] ?: 0) + 1
                    }
                    
                    groupMessageDao.updateReactions(messageId, gson.toJson(currentReactions), reaction)
                }
                
                emit(GroupResult.Success(Unit))
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در ثبت واکنش"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }


    suspend fun deleteMessage(groupId: String, messageId: String, deleteForEveryone: Boolean = true): Flow<GroupResult<Unit>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.deleteGroupMessage(groupId, messageId, deleteForEveryone)
            if (response.isSuccessful && response.body()?.success == true) {
                groupMessageDao.deleteMessageById(messageId)
                emit(GroupResult.Success(Unit))
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در حذف پیام"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    suspend fun editMessage(groupId: String, messageId: String, content: String): Flow<GroupResult<Message>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.editGroupMessage(groupId, messageId, EditMessageRequest(content))
            if (response.isSuccessful && response.body()?.success == true) {
                val messageDto = response.body()?.data
                if (messageDto != null) {
                    groupMessageDao.insertMessage(messageDto.toEntity())
                    emit(GroupResult.Success(messageDto.toDomain()))
                } else {
                    emit(GroupResult.Error("خطا در ویرایش پیام"))
                }
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در ویرایش پیام"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    suspend fun sendPollMessage(groupId: String, poll: PollDto): Flow<GroupResult<Message>> = flow {
        emit(GroupResult.Loading)
        try {
            val response = apiService.sendGroupMessage(
                groupId,
                SendGroupMessageRequest(
                    type = "POLL",
                    content = poll.question,
                    pollId = poll.id
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val messageDto = response.body()?.data
                if (messageDto != null) {
                    // CRITICAL FIX: ALWAYS use the input poll object to preserve all options
                    // Backend may return incomplete poll data (missing options)
                    val finalDto = messageDto.copy(poll = poll, type = "POLL")
                    groupMessageDao.insertMessage(finalDto.toEntity())
                    emit(GroupResult.Success(finalDto.toDomain()))
                }
            } else {
                emit(GroupResult.Error(response.body()?.message ?: "خطا در ارسال نظرسنجی"))
            }
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    suspend fun archiveGroup(groupId: String): Flow<GroupResult<Unit>> = flow {
        emit(GroupResult.Loading)
        try {
            apiService.archiveGroup(groupId, true)
            groupDao.updateArchiveStatus(groupId, true)
            emit(GroupResult.Success(Unit))
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در بایگانی گروه: ${e.message}"))
        }
    }
    
    suspend fun unarchiveGroup(groupId: String): Flow<GroupResult<Unit>> = flow {
        emit(GroupResult.Loading)
        try {
            apiService.archiveGroup(groupId, false)
            groupDao.updateArchiveStatus(groupId, false)
            emit(GroupResult.Success(Unit))
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در خروج از بایگانی: ${e.message}"))
        }
    }
    
    suspend fun togglePin(groupId: String, isPinned: Boolean): Flow<GroupResult<Unit>> = flow {
        emit(GroupResult.Loading)
        try {
            apiService.pinGroup(groupId, isPinned)
            groupDao.updatePinStatus(groupId, isPinned)
            emit(GroupResult.Success(Unit))
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در سنجاق کردن: ${e.message}"))
        }
    }

    suspend fun toggleMute(groupId: String, isMuted: Boolean): Flow<GroupResult<Unit>> = flow {
        emit(GroupResult.Loading)
        try {
            apiService.muteGroup(groupId, isMuted)
            groupDao.updateMuteStatus(groupId, isMuted)
            emit(GroupResult.Success(Unit))
        } catch (e: Exception) {
            emit(GroupResult.Error("خطا در تغییر وضعیت بیصدا: ${e.message}"))
        }
    }
    
    fun observeArchivedGroups(): Flow<List<Group>> = groupDao.observeArchivedGroups().map { groups ->
        groups.map { it.toDomain() }
    }.distinctUntilChanged()
}
