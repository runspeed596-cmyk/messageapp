package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SpecialFolderService(
    private val aiBotRepository: AiBotRepository,
    private val aiBotMessageRepository: AiBotMessageRepository,
    private val groupRepository: GroupRepository,
    private val channelRepository: ChannelRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val channelSubscriberRepository: ChannelSubscriberRepository,
    private val userRepository: UserRepository,
    private val roleChannelMappingRepository: RoleChannelMappingRepository,
    private val universityRepository: UniversityRepository
) {
    @Transactional
    fun getSpecialFolder(userId: UUID): SpecialFolderDto {
        val user: User = userRepository.findById(userId).orElseThrow {
            RuntimeException("User not found")
        }
        val userProvince: String? = user.profileDetails?.province
        val userCity: String? = user.profileDetails?.city
        val userUniversity: String? = user.profileDetails?.university
        val userFieldOfStudy: String? = user.profileDetails?.fieldOfStudy
        val userEducationLevel: String? = user.profileDetails?.education
        
        // Gather all ministries associated with user's universities
        val userMinistries: Set<String> = user.profileDetails?.universities?.mapNotNull { uniName ->
            universityRepository.findByNameIgnoreCase(uniName).firstOrNull()?.ministryName
        }?.toSet() ?: emptySet()

        // Auto-subscribe user to matching official channels/groups
        autoSubscribeUser(user)
        val aiBots: List<AiBotDto> = aiBotRepository
            .findAllByIsActiveTrue()
            .map { it.toDto() }
        val officialChannels: List<Channel> = channelRepository.findByIsOfficialTrue()
        val filteredChannels: List<SpecialChannelDto> = officialChannels
            .filter { channel ->
                if (channel.displayMode != OfficialDisplayMode.SPECIAL) return@filter false
                // Always show channels where user is admin/owner
                val isUserAdmin: Boolean = channelSubscriberRepository.findByChannelIdAndUserId(channel.id!!, userId)?.isAdmin == true
                    || channel.owner?.id == userId
                isUserAdmin || isChannelRelevantForUser(channel, userProvince, userCity, userUniversity, userFieldOfStudy, userEducationLevel, user.educationalRole, userMinistries)
            }
            .map { channel ->
                val subscriberCount: Long = channelSubscriberRepository.countByChannelId(channel.id!!)
                SpecialChannelDto(
                    id = channel.id.toString(),
                    name = channel.name,
                    avatarUrl = channel.avatarUrl,
                    category = channel.officialCategory?.name ?: "",
                    subscriberCount = subscriberCount.toInt()
                )
            }
        val officialGroups: List<Group> = groupRepository.findByIsOfficialTrue()
        val filteredGroups: List<SpecialGroupDto> = officialGroups
            .filter { group ->
                if (group.displayMode != OfficialDisplayMode.SPECIAL) return@filter false
                // Always show groups where user is admin/owner
                val member: GroupMember? = groupMemberRepository.findByGroupIdAndUserId(group.id!!, userId)
                val isUserAdmin: Boolean = member != null && (member.role == MemberRole.OWNER || member.role == MemberRole.ADMIN)
                isUserAdmin || isGroupRelevantForUser(group, userProvince, userCity, userUniversity, userFieldOfStudy, userEducationLevel, user.educationalRole, userMinistries)
            }
            .map { group ->
                val memberCount: Long = groupMemberRepository.countByGroupId(group.id!!)
                SpecialGroupDto(
                    id = group.id.toString(),
                    name = group.name,
                    avatarUrl = group.avatarUrl,
                    category = group.officialCategory?.name ?: "",
                    memberCount = memberCount.toInt()
                )
            }
        val isProfileComplete: Boolean = !userUniversity.isNullOrBlank()
            && !userFieldOfStudy.isNullOrBlank()
            && !userEducationLevel.isNullOrBlank()
        // Build support channels/groups lists
        val supportChannels: List<SpecialChannelDto> = officialChannels
            .filter { it.displayMode == OfficialDisplayMode.SUPPORT }
            .map { channel ->
                val subscriberCount: Long = channelSubscriberRepository.countByChannelId(channel.id!!)
                SpecialChannelDto(
                    id = channel.id.toString(),
                    name = channel.name,
                    avatarUrl = channel.avatarUrl,
                    category = channel.officialCategory?.name ?: "",
                    subscriberCount = subscriberCount.toInt()
                )
            }
        val supportGroups: List<SpecialGroupDto> = officialGroups
            .filter { it.displayMode == OfficialDisplayMode.SUPPORT }
            .map { group ->
                val memberCount: Long = groupMemberRepository.countByGroupId(group.id!!)
                SpecialGroupDto(
                    id = group.id.toString(),
                    name = group.name,
                    avatarUrl = group.avatarUrl,
                    category = group.officialCategory?.name ?: "",
                    memberCount = memberCount.toInt()
                )
            }
        return SpecialFolderDto(
            aiBots = aiBots,
            channels = filteredChannels,
            groups = filteredGroups,
            supportChannels = supportChannels,
            supportGroups = supportGroups,
            supportChatId = null,
            isProfileComplete = isProfileComplete
        )
    }

    // ── Auto-Subscribe Logic ─────────────────────────────────────────────────

    @Transactional
    fun autoSubscribeUser(user: User) {
        val userId: UUID = user.id ?: return
        val userProvince: String? = user.profileDetails?.province
        val userCity: String? = user.profileDetails?.city
        val userUniversity: String? = user.profileDetails?.university
        val userFieldOfStudy: String? = user.profileDetails?.fieldOfStudy
        val userEducationLevel: String? = user.profileDetails?.education
        val userRole = user.educationalRole
        val userGradeLevel = user.gradeLevel
        val userMajor = user.major

        // Gather all ministries associated with user's universities
        val userMinistries: Set<String> = user.profileDetails?.universities?.mapNotNull { uniName ->
            universityRepository.findByNameIgnoreCase(uniName).firstOrNull()?.ministryName
        }?.toSet() ?: emptySet()
        
        // Auto-join mandatory role-based channels
        if (!userRole.isNullOrBlank()) {
            val mappings = roleChannelMappingRepository.findByEducationalRole(userRole)
            for (mapping in mappings) {
                // Must match grade and major if specified in mapping
                if (mapping.gradeLevel != null && mapping.gradeLevel != userGradeLevel) continue
                if (mapping.major != null && mapping.major != userMajor) continue
                
                mapping.channel?.let { channel ->
                    val existingSub = channelSubscriberRepository.findByChannelIdAndUserId(channel.id!!, userId)
                    if (existingSub == null) {
                        val newSub = ChannelSubscriber(
                            channel = channel,
                            user = user,
                            isAdmin = false,
                            isMandatory = true
                        )
                        channelSubscriberRepository.save(newSub)
                    } else if (!existingSub.isMandatory) {
                        existingSub.isMandatory = true
                        channelSubscriberRepository.save(existingSub)
                    }
                    Unit
                }
            }
        }
        
        // Auto-join official channels
        val officialChannels: List<Channel> = channelRepository.findByIsOfficialTrue()
        for (channel: Channel in officialChannels) {
            if (!isChannelRelevantForUser(channel, userProvince, userCity, userUniversity, userFieldOfStudy, userEducationLevel, user.educationalRole, userMinistries)) continue
            val existingSub: ChannelSubscriber? = channelSubscriberRepository.findByChannelIdAndUserId(channel.id!!, userId)
            if (existingSub == null) {
                val newSub: ChannelSubscriber = ChannelSubscriber(
                    channel = channel,
                    user = user,
                    isAdmin = false
                )
                channelSubscriberRepository.save(newSub)
            }
        }
        // Auto-join official groups (except course groups)
        val officialGroups: List<Group> = groupRepository.findByIsOfficialTrue()
        for (group: Group in officialGroups) {
            if (group.officialCategory == OfficialGroupCategory.COURSE_GROUP) continue
            if (!isGroupRelevantForUser(group, userProvince, userCity, userUniversity, userFieldOfStudy, userEducationLevel, user.educationalRole, userMinistries)) continue
            val existingMember: GroupMember? = groupMemberRepository.findByGroupIdAndUserId(group.id!!, userId)
            if (existingMember == null) {
                val newMember: GroupMember = GroupMember(
                    group = group,
                    user = user,
                    role = MemberRole.MEMBER
                )
                groupMemberRepository.save(newMember)
            }
        }
    }

    /**
     * Check if a channel is relevant to a user based on targeting fields.
     * Each non-null targeting field must match the user's profile (AND logic).
     * If all targeting fields are null, the channel is public/unrestricted.
     */
    private fun isChannelRelevantForUser(
        channel: Channel,
        userProvince: String?,
        userCity: String?,
        userUniversity: String?,
        userFieldOfStudy: String?,
        userEducationLevel: String?,
        userEducationalRole: String?,
        userMinistries: Set<String> = emptySet()
    ): Boolean {
        if (channel.targetAudienceType != null) {
            val role = userEducationalRole?.uppercase() ?: ""
            if (channel.targetAudienceType == "STUDENT" && role != "UNI_STUDENT") return false
            if (channel.targetAudienceType == "PUPIL" && role != "SCHOOL_STUDENT") return false
        }
        if (channel.targetProvince != null) {
            if (userProvince == null || !channel.targetProvince!!.split(",").map { it.trim() }.contains(userProvince.trim())) return false
        }
        if (channel.targetCity != null) {
            if (userCity == null || !channel.targetCity!!.split(",").map { it.trim() }.contains(userCity.trim())) return false
        }
        if (channel.targetUniversity != null) {
            if (userUniversity == null || !channel.targetUniversity!!.split(",").map { it.trim() }.contains(userUniversity.trim())) return false
        }
        if (channel.targetFieldOfStudy != null) {
            if (userFieldOfStudy == null || !channel.targetFieldOfStudy!!.split(",").map { it.trim() }.contains(userFieldOfStudy.trim())) return false
        }
        if (channel.targetEducationLevel != null) {
            if (userEducationLevel == null || !channel.targetEducationLevel!!.split(",").map { it.trim() }.contains(userEducationLevel.trim())) return false
        }
        if (channel.targetMinistry != null) {
            val targets = channel.targetMinistry!!.split(",").map { it.trim() }
            if (userMinistries.none { targets.contains(it) }) return false
        }
        return true
    }

    /**
     * Check if a group is relevant to a user based on targeting fields.
     * Each non-null targeting field must match the user's profile (AND logic).
     * If all targeting fields are null, the group is public/unrestricted.
     */
    private fun isGroupRelevantForUser(
        group: Group,
        userProvince: String?,
        userCity: String?,
        userUniversity: String?,
        userFieldOfStudy: String?,
        userEducationLevel: String?,
        userEducationalRole: String?,
        userMinistries: Set<String> = emptySet()
    ): Boolean {
        if (group.targetAudienceType != null) {
            val role = userEducationalRole?.uppercase() ?: ""
            if (group.targetAudienceType == "STUDENT" && role != "UNI_STUDENT") return false
            if (group.targetAudienceType == "PUPIL" && role != "SCHOOL_STUDENT") return false
        }
        if (group.targetProvince != null) {
            if (userProvince == null || !group.targetProvince!!.split(",").map { it.trim() }.contains(userProvince.trim())) return false
        }
        if (group.targetCity != null) {
            if (userCity == null || !group.targetCity!!.split(",").map { it.trim() }.contains(userCity.trim())) return false
        }
        if (group.targetUniversity != null) {
            if (userUniversity == null || !group.targetUniversity!!.split(",").map { it.trim() }.contains(userUniversity.trim())) return false
        }
        if (group.targetFieldOfStudy != null) {
            if (userFieldOfStudy == null || !group.targetFieldOfStudy!!.split(",").map { it.trim() }.contains(userFieldOfStudy.trim())) return false
        }
        if (group.targetMinistry != null) {
            val targets = group.targetMinistry!!.split(",").map { it.trim() }
            if (userMinistries.none { targets.contains(it) }) return false
        }
        if (group.targetEducationLevel != null) {
            if (userEducationLevel == null || !group.targetEducationLevel!!.split(",").map { it.trim() }.contains(userEducationLevel.trim())) return false
        }
        return true
    }

    fun createAiBot(request: CreateAiBotRequest): AiBotDto {
        val bot: AiBot = AiBot(
            name = request.name,
            botType = request.botType,
            description = request.description,
            avatarUrl = request.avatarUrl
        )
        return aiBotRepository.save(bot).toDto()
    }

    fun deleteAiBot(botId: UUID) {
        aiBotRepository.deleteById(botId)
    }

    fun getAllAiBots(): List<AiBotDto> {
        return aiBotRepository.findAllByIsActiveTrue().map { it.toDto() }
    }

    fun createOfficialGroup(request: CreateOfficialGroupRequest, creatorId: UUID): SpecialGroupDto {
        val creator: User = userRepository.findById(creatorId).orElseGet {
            userRepository.findAll().firstOrNull()
                ?: throw RuntimeException("No users exist in the system")
        }
        val group: Group = Group(
            name = request.name,
            description = request.description,
            avatarUrl = request.avatarUrl,
            isPublic = true,
            isOfficial = true,
            isSystemOfficial = true,
            officialCategory = request.category,
            hideMembers = request.hideMembers,
            displayMode = try { OfficialDisplayMode.valueOf(request.displayMode) } catch (e: Exception) { OfficialDisplayMode.SPECIAL },
            targetFieldOfStudy = request.targetFieldOfStudy,
            targetEducationLevel = request.targetEducationLevel,
            targetProvince = request.targetProvince,
            targetCity = request.targetCity,
            targetUniversity = request.targetUniversity,
            targetMinistry = request.targetMinistry,
            targetAudienceType = request.targetAudienceType,
            createdBy = creator
        )
        val savedGroup: Group = groupRepository.save(group)
        val ownerMember: GroupMember = GroupMember(
            group = savedGroup,
            user = creator,
            role = MemberRole.OWNER
        )
        groupMemberRepository.save(ownerMember)
        // Add specified admins during creation
        request.adminIds?.forEach { adminIdStr ->
            val adminId: UUID = UUID.fromString(adminIdStr)
            if (adminId != creator.id) {
                val adminUser: User = userRepository.findById(adminId).orElse(null) ?: return@forEach
                val adminMember: GroupMember = GroupMember(
                    group = savedGroup,
                    user = adminUser,
                    role = MemberRole.ADMIN
                )
                groupMemberRepository.save(adminMember)
            }
        }
        return SpecialGroupDto(
            id = savedGroup.id.toString(),
            name = savedGroup.name,
            avatarUrl = savedGroup.avatarUrl,
            category = savedGroup.officialCategory?.name ?: "",
            memberCount = 1 + (request.adminIds?.size ?: 0)
        )
    }

    fun createOfficialChannel(request: CreateOfficialChannelRequest, creatorId: UUID): SpecialChannelDto {
        val creator: User = userRepository.findById(creatorId).orElseGet {
            userRepository.findAll().firstOrNull()
                ?: throw RuntimeException("No users exist in the system")
        }
        val channel: Channel = Channel(
            name = request.name,
            description = request.description,
            avatarUrl = request.avatarUrl,
            isPublic = true,
            isOfficial = true,
            isSystemOfficial = true,
            officialCategory = request.category,
            displayMode = try { OfficialDisplayMode.valueOf(request.displayMode) } catch (e: Exception) { OfficialDisplayMode.SPECIAL },
            targetFieldOfStudy = request.targetFieldOfStudy,
            targetEducationLevel = request.targetEducationLevel,
            targetProvince = request.targetProvince,
            targetCity = request.targetCity,
            targetUniversity = request.targetUniversity,
            targetMinistry = request.targetMinistry,
            targetAudienceType = request.targetAudienceType,
            owner = creator
        )
        val savedChannel: Channel = channelRepository.save(channel)
        val ownerSubscriber: ChannelSubscriber = ChannelSubscriber(
            channel = savedChannel,
            user = creator,
            isAdmin = true
        )
        channelSubscriberRepository.save(ownerSubscriber)
        // Add specified admins during creation
        request.adminIds?.forEach { adminIdStr ->
            val adminId: UUID = UUID.fromString(adminIdStr)
            if (adminId != creator.id) {
                val adminUser: User = userRepository.findById(adminId).orElse(null) ?: return@forEach
                val adminSub: ChannelSubscriber = ChannelSubscriber(
                    channel = savedChannel,
                    user = adminUser,
                    isAdmin = true
                )
                channelSubscriberRepository.save(adminSub)
            }
        }
        return SpecialChannelDto(
            id = savedChannel.id.toString(),
            name = savedChannel.name,
            avatarUrl = savedChannel.avatarUrl,
            category = savedChannel.officialCategory?.name ?: "",
            subscriberCount = 1 + (request.adminIds?.size ?: 0)
        )
    }

    fun addGroupAdmin(groupId: UUID, userId: UUID) {
        val group: Group = groupRepository.findById(groupId).orElseThrow {
            RuntimeException("Group not found")
        }
        if (!group.isOfficial) {
            throw RuntimeException("Group is not official")
        }
        val user: User = userRepository.findById(userId).orElseThrow {
            RuntimeException("User not found")
        }
        val existingMember: GroupMember? = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        if (existingMember != null) {
            existingMember.role = MemberRole.ADMIN
            groupMemberRepository.save(existingMember)
        } else {
            val newMember: GroupMember = GroupMember(
                group = group,
                user = user,
                role = MemberRole.ADMIN
            )
            groupMemberRepository.save(newMember)
        }
    }

    fun addChannelAdmin(channelId: UUID, userId: UUID) {
        val channel: Channel = channelRepository.findById(channelId).orElseThrow {
            RuntimeException("Channel not found")
        }
        if (!channel.isOfficial) {
            throw RuntimeException("Channel is not official")
        }
        val user: User = userRepository.findById(userId).orElseThrow {
            RuntimeException("User not found")
        }
        val existingSub: ChannelSubscriber? = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
        if (existingSub != null) {
            existingSub.isAdmin = true
            channelSubscriberRepository.save(existingSub)
        } else {
            val newSub: ChannelSubscriber = ChannelSubscriber(
                channel = channel,
                user = user,
                isAdmin = true
            )
            channelSubscriberRepository.save(newSub)
        }
    }

    // ── Official Listing & Management ────────────────────────────────────────

    @Transactional(readOnly = true)
    fun getAllOfficialChannels(): List<OfficialChannelAdminDto> {
        val channels: List<Channel> = channelRepository.findByIsSystemOfficialTrue()
        return channels.map { channel ->
            val subscriberCount: Long = channelSubscriberRepository.countByChannelId(channel.id!!)
            val admins: List<UserDto> = channelSubscriberRepository.findByChannelId(channel.id!!)
                .filter { it.isAdmin }
                .mapNotNull { it.user?.toDto() }
            OfficialChannelAdminDto(
                id = channel.id.toString(),
                name = channel.name,
                description = channel.description,
                avatarUrl = channel.avatarUrl,
                category = channel.officialCategory?.name ?: "",
                subscriberCount = subscriberCount.toInt(),
                admins = admins,
                displayMode = channel.displayMode.name,
                targetFieldOfStudy = channel.targetFieldOfStudy,
                targetEducationLevel = channel.targetEducationLevel,
                targetProvince = channel.targetProvince,
                targetCity = channel.targetCity,
                targetUniversity = channel.targetUniversity,
                targetMinistry = channel.targetMinistry,
                targetAudienceType = channel.targetAudienceType
            )
        }
    }

    @Transactional(readOnly = true)
    fun getAllAppCreatedChannels(): List<OfficialChannelAdminDto> {
        val channels: List<Channel> = channelRepository.findByIsSystemOfficialFalse().filter { it.isOfficial }
        return channels.map { channel ->
            val subscriberCount: Long = channelSubscriberRepository.countByChannelId(channel.id!!)
            val admins: List<UserDto> = channelSubscriberRepository.findByChannelId(channel.id!!)
                .filter { it.isAdmin }
                .mapNotNull { it.user?.toDto() }
            OfficialChannelAdminDto(
                id = channel.id.toString(),
                name = channel.name,
                description = channel.description,
                avatarUrl = channel.avatarUrl,
                category = channel.officialCategory?.name ?: "",
                subscriberCount = subscriberCount.toInt(),
                admins = admins,
                displayMode = channel.displayMode.name,
                targetFieldOfStudy = channel.targetFieldOfStudy,
                targetEducationLevel = channel.targetEducationLevel,
                targetProvince = channel.targetProvince,
                targetCity = channel.targetCity,
                targetUniversity = channel.targetUniversity,
                targetMinistry = channel.targetMinistry,
                targetAudienceType = channel.targetAudienceType
            )
        }
    }

    @Transactional(readOnly = true)
    fun getAllOfficialGroups(): List<OfficialGroupAdminDto> {
        val groups: List<Group> = groupRepository.findByIsSystemOfficialTrue()
        return groups.map { group ->
            val memberCount: Long = groupMemberRepository.countByGroupId(group.id!!)
            val admins: List<UserDto> = groupMemberRepository.findByGroupId(group.id!!)
                .filter { it.role == MemberRole.ADMIN || it.role == MemberRole.OWNER }
                .mapNotNull { it.user?.toDto() }
            OfficialGroupAdminDto(
                id = group.id.toString(),
                name = group.name,
                description = group.description,
                avatarUrl = group.avatarUrl,
                category = group.officialCategory?.name ?: "",
                hideMembers = group.hideMembers,
                memberCount = memberCount.toInt(),
                admins = admins,
                displayMode = group.displayMode.name,
                targetFieldOfStudy = group.targetFieldOfStudy,
                targetEducationLevel = group.targetEducationLevel,
                targetProvince = group.targetProvince,
                targetCity = group.targetCity,
                targetUniversity = group.targetUniversity,
                targetMinistry = group.targetMinistry,
                targetAudienceType = group.targetAudienceType
            )
        }
    }

    @Transactional(readOnly = true)
    fun getAllAppCreatedGroups(): List<OfficialGroupAdminDto> {
        val groups: List<Group> = groupRepository.findByIsSystemOfficialFalse().filter { it.isOfficial }
        return groups.map { group ->
            val memberCount: Long = groupMemberRepository.countByGroupId(group.id!!)
            val admins: List<UserDto> = groupMemberRepository.findByGroupId(group.id!!)
                .filter { it.role == MemberRole.ADMIN || it.role == MemberRole.OWNER }
                .mapNotNull { it.user?.toDto() }
            OfficialGroupAdminDto(
                id = group.id.toString(),
                name = group.name,
                description = group.description,
                avatarUrl = group.avatarUrl,
                category = group.officialCategory?.name ?: "",
                hideMembers = group.hideMembers,
                memberCount = memberCount.toInt(),
                admins = admins,
                displayMode = group.displayMode.name,
                targetFieldOfStudy = group.targetFieldOfStudy,
                targetEducationLevel = group.targetEducationLevel,
                targetProvince = group.targetProvince,
                targetCity = group.targetCity,
                targetUniversity = group.targetUniversity,
                targetMinistry = group.targetMinistry,
                targetAudienceType = group.targetAudienceType
            )
        }
    }

    @Transactional
    fun updateOfficialChannel(channelId: UUID, request: CreateOfficialChannelRequest): OfficialChannelAdminDto {
        val channel: Channel = channelRepository.findById(channelId).orElseThrow {
            RuntimeException("Channel not found")
        }
        if (!channel.isOfficial) {
            throw RuntimeException("Channel is not official")
        }
        channel.name = request.name
        channel.description = request.description
        channel.avatarUrl = request.avatarUrl
        channel.officialCategory = request.category
        channel.displayMode = try { OfficialDisplayMode.valueOf(request.displayMode) } catch (e: Exception) { OfficialDisplayMode.SPECIAL }
        channel.targetFieldOfStudy = request.targetFieldOfStudy
        channel.targetEducationLevel = request.targetEducationLevel
        channel.targetProvince = request.targetProvince
        channel.targetCity = request.targetCity
        channel.targetUniversity = request.targetUniversity
        channel.targetMinistry = request.targetMinistry
        channel.targetAudienceType = request.targetAudienceType
        val savedChannel: Channel = channelRepository.save(channel)
        val subscriberCount: Long = channelSubscriberRepository.countByChannelId(savedChannel.id!!)
        val admins: List<UserDto> = channelSubscriberRepository.findByChannelId(savedChannel.id!!)
            .filter { it.isAdmin }
            .mapNotNull { it.user?.toDto() }
        return OfficialChannelAdminDto(
            id = savedChannel.id.toString(),
            name = savedChannel.name,
            description = savedChannel.description,
            avatarUrl = savedChannel.avatarUrl,
            category = savedChannel.officialCategory?.name ?: "",
            subscriberCount = subscriberCount.toInt(),
            admins = admins,
            displayMode = savedChannel.displayMode.name,
            targetFieldOfStudy = savedChannel.targetFieldOfStudy,
            targetEducationLevel = savedChannel.targetEducationLevel,
            targetProvince = savedChannel.targetProvince,
            targetCity = savedChannel.targetCity,
            targetUniversity = savedChannel.targetUniversity,
            targetMinistry = savedChannel.targetMinistry,
            targetAudienceType = savedChannel.targetAudienceType
        )
    }

    @Transactional
    fun updateOfficialGroup(groupId: UUID, request: CreateOfficialGroupRequest): OfficialGroupAdminDto {
        val group: Group = groupRepository.findById(groupId).orElseThrow {
            RuntimeException("Group not found")
        }
        if (!group.isOfficial) {
            throw RuntimeException("Group is not official")
        }
        group.name = request.name
        group.description = request.description
        group.avatarUrl = request.avatarUrl
        group.officialCategory = request.category
        group.hideMembers = request.hideMembers
        group.displayMode = try { OfficialDisplayMode.valueOf(request.displayMode) } catch (e: Exception) { OfficialDisplayMode.SPECIAL }
        group.targetFieldOfStudy = request.targetFieldOfStudy
        group.targetEducationLevel = request.targetEducationLevel
        group.targetProvince = request.targetProvince
        group.targetCity = request.targetCity
        group.targetUniversity = request.targetUniversity
        group.targetMinistry = request.targetMinistry
        group.targetAudienceType = request.targetAudienceType
        val savedGroup: Group = groupRepository.save(group)
        val memberCount: Long = groupMemberRepository.countByGroupId(savedGroup.id!!)
        val admins: List<UserDto> = groupMemberRepository.findByGroupId(savedGroup.id!!)
            .filter { it.role == MemberRole.ADMIN || it.role == MemberRole.OWNER }
            .mapNotNull { it.user?.toDto() }
        return OfficialGroupAdminDto(
            id = savedGroup.id.toString(),
            name = savedGroup.name,
            description = savedGroup.description,
            avatarUrl = savedGroup.avatarUrl,
            category = savedGroup.officialCategory?.name ?: "",
            hideMembers = savedGroup.hideMembers,
            memberCount = memberCount.toInt(),
            admins = admins,
            displayMode = savedGroup.displayMode.name,
            targetFieldOfStudy = savedGroup.targetFieldOfStudy,
            targetEducationLevel = savedGroup.targetEducationLevel,
            targetProvince = savedGroup.targetProvince,
            targetCity = savedGroup.targetCity,
            targetUniversity = savedGroup.targetUniversity,
            targetMinistry = savedGroup.targetMinistry,
            targetAudienceType = savedGroup.targetAudienceType
        )
    }

    @Transactional
    fun deleteOfficialChannel(channelId: UUID) {
        val channel: Channel = channelRepository.findById(channelId).orElseThrow {
            RuntimeException("Channel not found")
        }
        if (!channel.isOfficial) {
            throw RuntimeException("Channel is not official")
        }
        channelRepository.delete(channel)
    }

    @Transactional
    fun deleteOfficialGroup(groupId: UUID) {
        val group: Group = groupRepository.findById(groupId).orElseThrow {
            RuntimeException("Group not found")
        }
        if (!group.isOfficial) {
            throw RuntimeException("Group is not official")
        }
        groupRepository.delete(group)
    }

    @Transactional(readOnly = true)
    fun getChannelAdmins(channelId: UUID): List<UserDto> {
        return channelSubscriberRepository.findByChannelId(channelId)
            .filter { it.isAdmin }
            .mapNotNull { it.user?.toDto() }
    }

    @Transactional(readOnly = true)
    fun getGroupAdmins(groupId: UUID): List<UserDto> {
        return groupMemberRepository.findByGroupId(groupId)
            .filter { it.role == MemberRole.ADMIN || it.role == MemberRole.OWNER }
            .mapNotNull { it.user?.toDto() }
    }

    @Transactional
    fun removeChannelAdmin(channelId: UUID, userId: UUID) {
        val sub: ChannelSubscriber? = channelSubscriberRepository.findByChannelIdAndUserId(channelId, userId)
        if (sub != null) {
            sub.isAdmin = false
            channelSubscriberRepository.save(sub)
        }
    }

    @Transactional
    fun removeGroupAdmin(groupId: UUID, userId: UUID) {
        val member: GroupMember? = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        if (member != null && member.role == MemberRole.ADMIN) {
            member.role = MemberRole.MEMBER
            groupMemberRepository.save(member)
        }
    }

    // ── AI Bot Chat ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    fun getAiBotMessages(botId: UUID, userId: UUID, page: Int = 0, size: Int = 50): List<AiBotMessageDto> {
        val pageable: org.springframework.data.domain.Pageable =
            org.springframework.data.domain.PageRequest.of(page, size)
        val messages: List<AiBotMessage> = aiBotMessageRepository
            .findByBotIdAndUserIdOrderByCreatedAtDesc(botId, userId, pageable)
        return messages.reversed().map { it.toDto() }
    }

    @Transactional
    fun sendAiBotMessage(botId: UUID, userId: UUID, content: String): List<AiBotMessageDto> {
        // Verify bot exists
        aiBotRepository.findById(botId).orElseThrow {
            RuntimeException("AI bot not found")
        }
        // Save user message
        val userMessage: AiBotMessage = AiBotMessage(
            botId = botId,
            userId = userId,
            content = content,
            role = "USER"
        )
        aiBotMessageRepository.save(userMessage)
        // Generate placeholder AI response
        val aiResponse: AiBotMessage = AiBotMessage(
            botId = botId,
            userId = userId,
            content = "سلام! من هنوز در حال توسعه هستم. به زودی می‌توانم به شما کمک کنم. 🤖",
            role = "ASSISTANT"
        )
        aiBotMessageRepository.save(aiResponse)
        return listOf(userMessage.toDto(), aiResponse.toDto())
    }
}
