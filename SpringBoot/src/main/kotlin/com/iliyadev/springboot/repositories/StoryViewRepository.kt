package com.iliyadev.springboot.repositories

import com.iliyadev.springboot.models.Story
import com.iliyadev.springboot.models.StoryView
import com.iliyadev.springboot.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StoryViewRepository : JpaRepository<StoryView, UUID> {
    
    fun existsByStoryAndUser(story: Story, user: User): Boolean
    
    fun findByStoryOrderByViewedAtDesc(story: Story): List<StoryView>
    
    fun countByStory(story: Story): Int
    
    fun deleteByStory(story: Story)
}
