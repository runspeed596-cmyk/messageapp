package com.iliyadev.springboot.repositories

import com.iliyadev.springboot.models.ElmEvent
import com.iliyadev.springboot.models.ElmEventType
import com.iliyadev.springboot.models.EventReport
import com.iliyadev.springboot.models.StartupIdea
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ElmEventRepository : JpaRepository<ElmEvent, UUID> {
    fun findAllByType(type: ElmEventType): List<ElmEvent>
}

@Repository
interface StartupIdeaRepository : JpaRepository<StartupIdea, UUID>

@Repository
interface EventReportRepository : JpaRepository<EventReport, UUID> {
    fun findAllByIsVerified(isVerified: Boolean): List<EventReport>
}
