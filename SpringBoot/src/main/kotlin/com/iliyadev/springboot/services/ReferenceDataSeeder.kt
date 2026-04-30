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
    private val fieldOfStudyRepository: FieldOfStudyRepository
) {
    private val logger = LoggerFactory.getLogger(ReferenceDataSeeder::class.java)

    @PostConstruct
    fun seed() {
        seedRoles()
        seedLevels()
    }

    private fun seedRoles() {
        val requiredRoles = listOf("SCHOOL_STUDENT", "UNI_STUDENT", "TEACHER", "FREELANCER")
        val existingRoles = educationalRoleOptionRepository.findAll().map { it.valueEn }
        
        if (!existingRoles.containsAll(requiredRoles)) {
            val roles = listOf(
                EducationalRoleOption(labelFa = "دانش‌آموز", valueEn = "SCHOOL_STUDENT", emoji = "🎒", displayOrder = 1),
                EducationalRoleOption(labelFa = "دانشجو", valueEn = "UNI_STUDENT", emoji = "🎓", displayOrder = 2),
                EducationalRoleOption(labelFa = "استاد/معلم", valueEn = "TEACHER", emoji = "👨‍🏫", displayOrder = 3),
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
}
