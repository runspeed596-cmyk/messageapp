package com.iliyadev.springboot.repositories

import com.iliyadev.springboot.models.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HomeBannerRepository : JpaRepository<HomeBanner, UUID> {
    fun findAllByOrderByDisplayOrderAsc(): List<HomeBanner>
    fun findAllByIsActiveTrueOrderByDisplayOrderAsc(): List<HomeBanner>
    fun findAllBySectionAndIsActiveTrueOrderByDisplayOrderAsc(section: String): List<HomeBanner>
}

@Repository
interface ClubRepository : JpaRepository<Club, UUID> {
    fun findAllByOrderByDisplayOrderAsc(): List<Club>
}

@Repository
interface StudentOrgRepository : JpaRepository<StudentOrg, UUID> {
    fun findAllByOrderByDisplayOrderAsc(): List<StudentOrg>
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
    fun findByBotType(botType: String): AiBot?
}

@Repository
interface FieldOfStudyRepository : JpaRepository<FieldOfStudy, UUID> {
    fun findAllByOrderByDisplayOrderAsc(): List<FieldOfStudy>
    fun findByNameIgnoreCaseAndEducationLevelIgnoreCase(name: String, educationLevel: String): List<FieldOfStudy>
}

@Repository
interface FacultyRepository : JpaRepository<Faculty, UUID> {
    fun findAllByOrderByDisplayOrderAsc(): List<Faculty>
    fun findByEducationLevelIgnoreCase(educationLevel: String): List<Faculty>
    fun findByNameIgnoreCase(name: String): List<Faculty>
}

@Repository
interface EducationLevelRepository : JpaRepository<EducationLevel, UUID> {
    fun findAllByOrderByDisplayOrderAsc(): List<EducationLevel>
}

@Repository
interface EducationalRoleOptionRepository : JpaRepository<EducationalRoleOption, UUID> {
    fun findAllByOrderByDisplayOrderAsc(): List<EducationalRoleOption>
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

@Repository
interface PanelAdminRepository : JpaRepository<PanelAdmin, UUID> {
    fun findByUsername(username: String): PanelAdmin?
    fun existsByUsername(username: String): Boolean
}
