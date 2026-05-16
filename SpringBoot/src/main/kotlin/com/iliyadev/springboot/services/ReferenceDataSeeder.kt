package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReferenceDataSeeder(
    private val educationalRoleOptionRepository: EducationalRoleOptionRepository,
    private val educationLevelRepository: EducationLevelRepository,
    private val fieldOfStudyRepository: FieldOfStudyRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(ReferenceDataSeeder::class.java)

    @PostConstruct
    fun seed() {
        try {
            seedRoles()
            seedLevels()
            seedBotUser()
            logger.info("✅ Reference data seeding completed successfully")
        } catch (e: Exception) {
            // Do NOT crash the entire application if seeding fails
            // Data likely already exists from a previous deployment
            logger.warn("⚠️ Reference data seeding skipped (data likely already exists): ${e.message}")
        }
    }

    private fun seedRoles() {
        val requiredRoles = listOf("SCHOOL_STUDENT", "UNI_STUDENT", "TEACHER", "FREELANCER")
        val existingRoles = educationalRoleOptionRepository.findAll().map { it.valueEn }
        
        if (!existingRoles.containsAll(requiredRoles)) {
            val roles = listOf(
                EducationalRoleOption(labelFa = "دانش‌آموز", valueEn = "SCHOOL_STUDENT", emoji = "🎒", displayOrder = 1),
                EducationalRoleOption(labelFa = "دانشجو", valueEn = "UNI_STUDENT", emoji = "🎓", displayOrder = 2),
                EducationalRoleOption(labelFa = "استاد", valueEn = "TEACHER", emoji = "👨‍🏫", displayOrder = 3),
                EducationalRoleOption(labelFa = "آزاد", valueEn = "FREELANCER", emoji = "💼", displayOrder = 4)
            )
            educationalRoleOptionRepository.saveAll(roles)
            logger.info("Seeded 4 educational roles")
        }
    }

    private fun seedLevels() {
        if (educationLevelRepository.count() == 0L) {
            val levels = listOf(
                // School levels
                EducationLevel(name = "متوسطه اول", roleValueEn = "SCHOOL_STUDENT", hasFieldOfStudy = false, hasFaculty = false, displayOrder = 1),
                EducationLevel(name = "متوسطه دوم (نظری)", roleValueEn = "SCHOOL_STUDENT", hasFieldOfStudy = true, hasFaculty = false, displayOrder = 2),
                EducationLevel(name = "هنرستان", roleValueEn = "SCHOOL_STUDENT", hasFieldOfStudy = true, hasFaculty = false, displayOrder = 3),
                
                // University levels
                EducationLevel(name = "کاردانی", roleValueEn = "UNI_STUDENT", hasFieldOfStudy = true, hasFaculty = true, displayOrder = 4),
                EducationLevel(name = "کارشناسی", roleValueEn = "UNI_STUDENT", hasFieldOfStudy = true, hasFaculty = true, displayOrder = 5),
                EducationLevel(name = "کارشناسی ارشد", roleValueEn = "UNI_STUDENT", hasFieldOfStudy = true, hasFaculty = true, displayOrder = 6),
                EducationLevel(name = "دکتری", roleValueEn = "UNI_STUDENT", hasFieldOfStudy = true, hasFaculty = true, displayOrder = 7)
            )
            educationLevelRepository.saveAll(levels)
            logger.info("Seeded role-linked education levels")
            
            // Seed some fields for هنرستان
            if (fieldOfStudyRepository.count() == 0L) {
                val fields = listOf(
                    FieldOfStudy(name = "شبکه و نرم‌افزار", educationLevel = "هنرستان", displayOrder = 1),
                    FieldOfStudy(name = "گرافیک", educationLevel = "هنرستان", displayOrder = 2),
                    FieldOfStudy(name = "حسابداری", educationLevel = "هنرستان", displayOrder = 3),
                    FieldOfStudy(name = "مهندسی کامپیوتر", educationLevel = "کارشناسی", displayOrder = 1),
                    FieldOfStudy(name = "حقوق", educationLevel = "کارشناسی", displayOrder = 2)
                )
                fieldOfStudyRepository.saveAll(fields)
                logger.info("Seeded basic fields of study")
            }
        }
    }

    private fun seedBotUser() {
        val botId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")
        if (!userRepository.existsById(botId)) {
            val botUser = User().apply {
                this.id = botId
                this.displayName = "اطلاع‌رسانی مثبت علم"
                this.username = "mosbat_elm_bot"
                this.phoneNumber = "0000000000"
                this.createdAt = java.time.Instant.now()
                this.isOnline = true
                this.bio = "سامانه اطلاع‌رسانی و یادآوری دوره‌های آموزشی مثبت علم"
            }
            userRepository.save(botUser)
            logger.info("Seeded Mosbat Elm notification bot user")
        } else {
            logger.info("Bot user already exists, skipping seed")
        }
    }
}
