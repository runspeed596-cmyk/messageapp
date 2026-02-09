package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.ElmEventRepository
import com.iliyadev.springboot.repositories.EventReportRepository
import com.iliyadev.springboot.repositories.StartupIdeaRepository
import com.iliyadev.springboot.repositories.UniversityRepository
import com.iliyadev.springboot.repositories.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ElmPeakService(
    private val eventRepository: ElmEventRepository,
    private val ideaRepository: StartupIdeaRepository,
    private val reportRepository: EventReportRepository,
    private val userRepository: UserRepository,
    private val universityRepository: UniversityRepository
) {

    fun getUniversities(): List<University> = universityRepository.findAll()

    fun getElmPeakData(): ElmPeakResponse {
        val allEvents = eventRepository.findAll().filter { it.isApproved }
        val competitions = allEvents.filter { it.type == ElmEventType.COMPETITION }.map { it.toDto() }
        val startups = allEvents.filter { it.type == ElmEventType.STARTUP }.map { it.toDto() }
        val congresses = allEvents.filter { it.type == ElmEventType.CONGRESS }.map { it.toDto() }
        
        return ElmPeakResponse(
            featuredEvents = competitions.take(3),
            competitions = competitions,
            startups = startups,
            congresses = congresses
        )
    }

    fun getPendingEvents(): List<ElmEventDto> {
        return eventRepository.findAll().filter { !it.isApproved }.map { it.toDto() }
    }

    @Transactional
    fun approveEvent(eventId: UUID) {
        val event = eventRepository.findById(eventId).orElseThrow { RuntimeException("Event not found") }
        event.isApproved = true
        eventRepository.save(event)
    }

    @Transactional
    fun rejectEvent(eventId: UUID) {
        eventRepository.deleteById(eventId)
    }

    @Transactional
    fun saveEvent(dto: ElmEventDto): ElmEventDto {
        val event = ElmEvent(
            id = dto.id ?: UUID.randomUUID(),
            title = dto.title,
            description = dto.description,
            date = dto.date,
            location = dto.location,
            imageUrl = dto.imageUrl,
            organizer = dto.organizer,
            reward = dto.reward,
            type = dto.type,
            isExternal = dto.isExternal,
            link = dto.link,
            isApproved = dto.isApproved
        )
        return eventRepository.save(event).toDto()
    }

    @Transactional
    fun submitUserEvent(userId: UUID, request: EventReportRequest): ElmEventDto {
        // Create event with isApproved=false for admin approval
        // Let Hibernate auto-generate the UUID (don't set id manually)
        val event = ElmEvent(
            title = request.title,
            description = request.description,
            date = request.date,
            location = request.location,
            link = request.link,
            type = request.type, // Use dynamic type from request
            isApproved = false, // Needs admin approval
            submittedByUserId = userId
        )
        return eventRepository.save(event).toDto()
    }

    @Transactional
    fun submitIdea(userId: UUID, request: IdeaSubmissionRequest): ElmEventDto {
        // Map Idea to ElmEvent (Startup type) so it appears in Admin Panel
        val event = ElmEvent(
            title = request.title,
            description = "${request.description}\n\nContact: ${request.contactInfo}",
            date = "Now", // Startup ideas don't have a date
            location = "Online", // Startup ideas don't have location
            link = "",
            type = ElmEventType.STARTUP,
            isApproved = false,
            submittedByUserId = userId
        )
        return eventRepository.save(event).toDto()
    }

    @Transactional
    fun reportEvent(userId: UUID, request: EventReportRequest): EventReport {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }
        val report = EventReport(
            title = request.title,
            description = request.description,
            date = request.date,
            location = request.location,
            link = request.link,
            user = user
        )
        return reportRepository.save(report)
    }

    private fun ElmEvent.toDto() = ElmEventDto(
        id = this.id,
        title = this.title,
        description = this.description,
        date = this.date,
        location = this.location,
        imageUrl = this.imageUrl,
        organizer = this.organizer,
        reward = this.reward,
        type = this.type,
        isExternal = this.isExternal,
        link = this.link,
        isApproved = this.isApproved
    )
}
