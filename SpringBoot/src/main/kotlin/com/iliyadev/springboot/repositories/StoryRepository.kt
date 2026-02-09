package com.iliyadev.springboot.repositories

import com.iliyadev.springboot.models.Story
import com.iliyadev.springboot.models.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface StoryRepository : JpaRepository<Story, UUID> {
    
    // Find active PERSONAL stories for specific users (active = not expired, excludes group/channel stories)
    @Query("SELECT s FROM Story s WHERE s.user IN :users AND s.expiresAt > :now AND s.group IS NULL AND s.channel IS NULL ORDER BY s.createdAt ASC")
    fun findActiveStoriesByUsers(
        @Param("users") users: List<User>,
        @Param("now") now: Instant
    ): List<Story>

    // Find active PERSONAL stories for a single user (excludes group/channel stories)
    @Query("SELECT s FROM Story s WHERE s.user = :user AND s.expiresAt > :now AND s.group IS NULL AND s.channel IS NULL ORDER BY s.createdAt ASC")
    fun findActiveStoriesByUser(
        @Param("user") user: User,
        @Param("now") now: Instant
    ): List<Story>

    @Query("SELECT s FROM Story s WHERE s.group IN :groups AND s.expiresAt > :now ORDER BY s.createdAt ASC")
    fun findActiveStoriesByGroups(
        @Param("groups") groups: List<com.iliyadev.springboot.models.Group>,
        @Param("now") now: Instant
    ): List<Story>

    @Query("SELECT s FROM Story s WHERE s.channel IN :channels AND s.expiresAt > :now ORDER BY s.createdAt ASC")
    fun findActiveStoriesByChannels(
        @Param("channels") channels: List<com.iliyadev.springboot.models.Channel>,
        @Param("now") now: Instant
    ): List<Story>

    @Query("SELECT COUNT(s) FROM Story s WHERE s.group = :group AND s.expiresAt > :now")
    fun countActiveStoriesByGroup(@Param("group") group: com.iliyadev.springboot.models.Group, @Param("now") now: Instant): Long

    @Query("SELECT COUNT(s) FROM Story s WHERE s.channel = :channel AND s.expiresAt > :now")
    fun countActiveStoriesByChannel(@Param("channel") channel: com.iliyadev.springboot.models.Channel, @Param("now") now: Instant): Long
}
