package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/admin/reference-data")
class AdminReferenceDataController(
    private val clubRepository: ClubRepository,
    private val studentOrgRepository: StudentOrgRepository,
    private val fieldOfStudyRepository: FieldOfStudyRepository,
    private val universityRepository: UniversityRepository,
    private val homeBannerRepository: HomeBannerRepository
) {
    @PostMapping("/clubs")
    fun addClub(@RequestBody club: Club): ResponseEntity<Club> {
        return ResponseEntity.ok(clubRepository.save(club))
    }

    @PostMapping("/student-orgs")
    fun addStudentOrg(@RequestBody org: StudentOrg): ResponseEntity<StudentOrg> {
        return ResponseEntity.ok(studentOrgRepository.save(org))
    }

    @PostMapping("/banners")
    fun addBanner(@RequestBody banner: HomeBanner): ResponseEntity<HomeBanner> {
        return ResponseEntity.ok(homeBannerRepository.save(banner))
    }
    
    @PostMapping("/fields-of-study")
    fun addField(@RequestBody field: FieldOfStudy): ResponseEntity<FieldOfStudy> {
        return ResponseEntity.ok(fieldOfStudyRepository.save(field))
    }
}
