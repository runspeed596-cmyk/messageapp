package com.iliyadev.springboot.repositories

import com.iliyadev.springboot.models.AdRequest
import com.iliyadev.springboot.models.AdRequestStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AdRepository : JpaRepository<AdRequest, UUID> {
    fun findByStatusOrderByCreatedAtDesc(status: AdRequestStatus): List<AdRequest>
    fun findAllByOrderByCreatedAtDesc(): List<AdRequest>
    fun findByRequesterId(requesterId: UUID): List<AdRequest>
}
