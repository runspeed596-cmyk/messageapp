package com.iliyadev.springboot.repositories

import com.iliyadev.springboot.models.Feedback
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FeedbackRepository : JpaRepository<Feedback, UUID> {
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Feedback>
}
