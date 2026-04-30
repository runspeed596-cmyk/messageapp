package com.iliyadev.springboot.config

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime

@Configuration
class MosbatElmDataSeeder(
    private val clubRepository: ClubRepository,
    private val studentOrgRepository: StudentOrgRepository,
    private val fieldOfStudyRepository: FieldOfStudyRepository,
    private val universityRepository: UniversityRepository,
    private val homeBannerRepository: HomeBannerRepository
) {
    @Bean
    fun seedMosbatElmData(): CommandLineRunner {
        return CommandLineRunner {
            if (clubRepository.count() == 0L) {
                clubRepository.saveAll(listOf(
                    Club(name = "کانون هلال احمر"),
                    Club(name = "کانون موسیقی"),
                    Club(name = "کانون تئاتر"),
                    Club(name = "کانون خیریه")
                ))
            }
            
            if (studentOrgRepository.count() == 0L) {
                studentOrgRepository.saveAll(listOf(
                    StudentOrg(name = "بسیج دانشجویی"),
                    StudentOrg(name = "انجمن اسلامی"),
                    StudentOrg(name = "شورای صنفی")
                ))
            }
            
            if (fieldOfStudyRepository.count() == 0L) {
                fieldOfStudyRepository.saveAll(listOf(
                    FieldOfStudy(name = "مهندسی کامپیوتر"),
                    FieldOfStudy(name = "مهندسی برق"),
                    FieldOfStudy(name = "روانشناسی"),
                    FieldOfStudy(name = "مدیریت")
                ))
            }
            
            if (homeBannerRepository.count() == 0L) {
                homeBannerRepository.saveAll(listOf(
                    HomeBanner(
                        title = "جشنواره تابستانه مثبت علم",
                        imageUrl = "https://example.com/banner1.jpg",
                        linkUrl = "https://kelasor.com/promo1",
                        section = "MOSBAT_ELM"
                    ),
                    HomeBanner(
                        title = "دوره جامع برنامه نویسی کاتلین",
                        imageUrl = "https://example.com/banner2.jpg",
                        linkUrl = "https://kelasor.com/kotlin",
                        section = "MOSBAT_ELM"
                    )
                ))
            }
        }
    }
}
