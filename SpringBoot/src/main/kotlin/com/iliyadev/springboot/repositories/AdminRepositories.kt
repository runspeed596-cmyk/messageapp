package com.iliyadev.springboot.repositories

import com.iliyadev.springboot.models.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HomeBannerRepository : JpaRepository<HomeBanner, UUID> {
    fun findAllByOrderByCreatedAtDesc(): List<HomeBanner>
    fun findAllByIsActiveTrueOrderByCreatedAtDesc(): List<HomeBanner>
    fun findAllBySectionAndIsActiveTrueOrderByCreatedAtDesc(section: String): List<HomeBanner>
}

@Repository
interface ClubRepository : JpaRepository<Club, UUID>

@Repository
interface StudentOrgRepository : JpaRepository<StudentOrg, UUID>

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
    fun findAllByIsActiveTrue(): List<AiBot>
    fun findByBotType(botType: String): AiBot?
}

@Repository
interface FieldOfStudyRepository : JpaRepository<FieldOfStudy, UUID> {
    fun findByNameIgnoreCaseAndEducationLevelIgnoreCase(name: String, educationLevel: String): List<FieldOfStudy>
}

@Repository
interface FacultyRepository : JpaRepository<Faculty, UUID> {
    fun findByEducationLevelIgnoreCase(educationLevel: String): List<Faculty>
    fun findByNameIgnoreCase(name: String): List<Faculty>
}

@Repository
interface EducationLevelRepository : JpaRepository<EducationLevel, UUID>

@Repository
interface EducationalRoleOptionRepository : JpaRepository<EducationalRoleOption, UUID>

@Repository
interface AiBotMessageRepository : JpaRepository<AiBotMessage, UUID> {
    fun findByBotIdAndUserIdOrderByCreatedAtAsc(botId: UUID, userId: UUID): List<AiBotMessage>
    fun findByBotIdAndUserIdOrderByCreatedAtDesc(
        botId: UUID,
        userId: UUID,
        pageable: org.springframework.data.domain.Pageable
    ): List<AiBotMessage>
}

@Repository
interface PanelAdminRepository : JpaRepository<PanelAdmin, UUID> {
    fun findByUsername(username: String): PanelAdmin?
    fun existsByUsername(username: String): Boolean
}
