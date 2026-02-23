package com.iliyadev.springboot.repositories

import com.iliyadev.springboot.models.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HomeBannerRepository : JpaRepository<HomeBanner, UUID> {
    fun findAllByOrderByDisplayOrderAsc(): List<HomeBanner>
    fun findAllByIsActiveTrueOrderByDisplayOrderAsc(): List<HomeBanner>
}

@Repository
interface UniversityRepository : JpaRepository<University, UUID> {
    fun findByNameContainingIgnoreCase(name: String): List<University>
    fun findByNameIgnoreCase(name: String): List<University>
}

@Repository
interface DiscountRepository : JpaRepository<Discount, UUID> {
    fun findAllByOrderByCreatedAtDesc(): List<Discount>
}

@Repository
interface EntertainmentMovieRepository : JpaRepository<EntertainmentMovie, UUID> {
    fun findAllByIsActiveTrue(): List<EntertainmentMovie>
}

@Repository
interface EntertainmentMusicRepository : JpaRepository<EntertainmentMusic, UUID> {
    fun findAllByIsActiveTrue(): List<EntertainmentMusic>
}

@Repository
interface EntertainmentRiddleRepository : JpaRepository<EntertainmentRiddle, UUID> {
    fun findAllByIsActiveTrue(): List<EntertainmentRiddle>
}

@Repository
interface RiddleOptionRepository : JpaRepository<RiddleOption, UUID>

@Repository
interface AiBotRepository : JpaRepository<AiBot, UUID> {
    fun findAllByIsActiveTrueOrderByDisplayOrderAsc(): List<AiBot>
}

@Repository
interface FieldOfStudyRepository : JpaRepository<FieldOfStudy, UUID> {
    fun findAllByOrderByDisplayOrderAsc(): List<FieldOfStudy>
    fun findByNameIgnoreCaseAndEducationLevelIgnoreCase(name: String, educationLevel: String): List<FieldOfStudy>
}

@Repository
interface FacultyRepository : JpaRepository<Faculty, UUID> {
    fun findAllByOrderByDisplayOrderAsc(): List<Faculty>
    fun findByNameIgnoreCase(name: String): List<Faculty>
}

@Repository
interface EducationLevelRepository : JpaRepository<EducationLevel, UUID> {
    fun findAllByOrderByDisplayOrderAsc(): List<EducationLevel>
}

@Repository
interface AiBotMessageRepository : JpaRepository<AiBotMessage, UUID> {
    fun findByBotIdAndUserIdOrderByCreatedAtAsc(botId: UUID, userId: UUID): List<AiBotMessage>
    fun findByBotIdAndUserIdOrderByCreatedAtDesc(
        botId: UUID,
        userId: UUID,
        pageable: org.springframework.data.domain.Pageable
    ): List<AiBotMessage>
}
