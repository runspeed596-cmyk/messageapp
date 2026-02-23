package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.stereotype.Service
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// Response DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class SmartFolderResponse(
    val folderType: FolderType,
    val labelFa: String,
    val iconName: String?,
    val channels: List<SmartFolderChannelResponse>
)

data class SmartFolderChannelResponse(
    val id: UUID,
    val name: String,
    val avatarUrl: String?,
    val subscriberCount: Long,
    val isVerifiedTeacher: Boolean,
    val classification: ChannelClassification,
    val isSubscribed: Boolean
)

@Service
class SmartFolderEngineService(
    private val smartFolderRuleRepository: SmartFolderRuleRepository,
    private val channelRepository: ChannelRepository,
    private val channelSubscriberRepository: ChannelSubscriberRepository,
    private val userRepository: UserRepository,
    private val userProfileDetailsRepository: UserProfileDetailsRepository,
    private val institutionRepository: InstitutionRepository
) {
    /**
     * Compute smart folders dynamically for the given user.
     * Each folder is populated based on channel classifications and user profile.
     */
    fun computeSmartFolders(userId: UUID): List<SmartFolderResponse> {
        val rules: List<SmartFolderRule> = smartFolderRuleRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
        if (rules.isEmpty()) return getDefaultFolders(userId)
        return rules.groupBy { it.folderType }.map { (folderType, folderRules) ->
            val firstRule: SmartFolderRule = folderRules.first()
            val classifications: List<ChannelClassification> = folderRules.map { it.classification }
            val channels: List<Channel> = findChannelsByClassifications(classifications, userId)
            SmartFolderResponse(
                folderType = folderType,
                labelFa = firstRule.labelFa,
                iconName = firstRule.iconName,
                channels = channels.map { mapChannelToResponse(it, userId) }
            )
        }
    }

    private fun getDefaultFolders(userId: UUID): List<SmartFolderResponse> {
        val result: MutableList<SmartFolderResponse> = mutableListOf()
        // Teachers folder
        val teacherChannels: List<Channel> = findChannelsByClassifications(
            listOf(ChannelClassification.VERIFIED_TEACHER), userId
        )
        result.add(SmartFolderResponse(
            folderType = FolderType.TEACHERS,
            labelFa = "اساتید",
            iconName = "school",
            channels = teacherChannels.map { mapChannelToResponse(it, userId) }
        ))
        // Elm Club folder
        val elmClubChannels: List<Channel> = findChannelsByClassifications(
            listOf(ChannelClassification.ELM_CLUB_INSTITUTION), userId
        )
        result.add(SmartFolderResponse(
            folderType = FolderType.ELM_CLUB,
            labelFa = "باشگاه علم",
            iconName = "groups",
            channels = elmClubChannels.map { mapChannelToResponse(it, userId) }
        ))
        // Courses folder
        val courseChannels: List<Channel> = findChannelsByClassifications(
            listOf(ChannelClassification.COURSE_CHANNEL), userId
        )
        result.add(SmartFolderResponse(
            folderType = FolderType.COURSES,
            labelFa = "دوره‌ها",
            iconName = "menu_book",
            channels = courseChannels.map { mapChannelToResponse(it, userId) }
        ))
        return result
    }

    private fun findChannelsByClassifications(
        classifications: List<ChannelClassification>,
        userId: UUID
    ): List<Channel> {
        return channelRepository.findByClassificationIn(classifications)
            .filter { it.isPublic }
    }

    private fun mapChannelToResponse(channel: Channel, userId: UUID): SmartFolderChannelResponse {
        val subscriberCount: Long = channelSubscriberRepository.countByChannelId(channel.id!!)
        val isSubscribed: Boolean = channelSubscriberRepository.existsByChannelIdAndUserId(channel.id!!, userId)
        return SmartFolderChannelResponse(
            id = channel.id!!,
            name = channel.name,
            avatarUrl = channel.avatarUrl,
            subscriberCount = subscriberCount,
            isVerifiedTeacher = channel.isVerifiedTeacher,
            classification = channel.classification,
            isSubscribed = isSubscribed
        )
    }
}

