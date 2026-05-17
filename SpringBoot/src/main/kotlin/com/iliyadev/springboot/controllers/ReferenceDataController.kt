package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reference-data")
class ReferenceDataController(
    private val universityRepository: UniversityRepository,
    private val fieldOfStudyRepository: FieldOfStudyRepository,
    private val educationLevelRepository: EducationLevelRepository,
    private val facultyRepository: FacultyRepository,
    private val educationalRoleOptionRepository: EducationalRoleOptionRepository,
    private val clubRepository: ClubRepository,
    private val studentOrgRepository: StudentOrgRepository
) {
    @GetMapping
    fun getReferenceData(): ResponseEntity<ApiResponse<ReferenceDataDto>> {
        val universities: List<UniversitySimpleDto> = universityRepository.findAll().map { it.toSimpleDto() }
        val fieldsOfStudy: List<FieldOfStudyDto> = fieldOfStudyRepository.findAll().map { it.toDto() }
        val educationLevels: List<EducationLevelDto> = educationLevelRepository.findAll().map { it.toDto() }
        val faculties: List<FacultyDto> = facultyRepository.findAll().map { it.toDto() }
        val educationalRoles: List<EducationalRoleOptionDto> = educationalRoleOptionRepository.findAll().map { it.toDto() }
        val clubs: List<ClubDto> = clubRepository.findAll().map { it.toDto() }
        val studentOrgs: List<StudentOrgDto> = studentOrgRepository.findAll().map { it.toDto() }
        
        val data = ReferenceDataDto(
            universities = universities,
            fieldsOfStudy = fieldsOfStudy,
            educationLevels = educationLevels,
            faculties = faculties,
            educationalRoles = educationalRoles,
            clubs = clubs,
            studentOrgs = studentOrgs
        )
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = data))
    }

    @GetMapping("/fields-by-level")
    fun getFieldsByLevel(@RequestParam level: String): ResponseEntity<ApiResponse<List<FieldOfStudyDto>>> {
        val allFields: List<FieldOfStudyDto> = fieldOfStudyRepository.findAll()
            .filter { it.educationLevel.equals(level, ignoreCase = true) }
            .map { it.toDto() }
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = allFields))
    }
}
