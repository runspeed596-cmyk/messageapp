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
    private val facultyRepository: FacultyRepository
) {
    @GetMapping
    fun getReferenceData(): ResponseEntity<ApiResponse<ReferenceDataDto>> {
        val universities: List<UniversitySimpleDto> = universityRepository.findAll().map { it.toSimpleDto() }
        val fieldsOfStudy: List<FieldOfStudyDto> = fieldOfStudyRepository.findAllByOrderByDisplayOrderAsc().map { it.toDto() }
        val educationLevels: List<EducationLevelDto> = educationLevelRepository.findAllByOrderByDisplayOrderAsc().map { it.toDto() }
        val faculties: List<FacultyDto> = facultyRepository.findAllByOrderByDisplayOrderAsc().map { it.toDto() }
        val data = ReferenceDataDto(
            universities = universities,
            fieldsOfStudy = fieldsOfStudy,
            educationLevels = educationLevels,
            faculties = faculties
        )
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = data))
    }
}
