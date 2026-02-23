package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import com.iliyadev.springboot.services.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/admin")
class AdminManagementController(
    private val userRepository: UserRepository,
    private val bannerRepository: HomeBannerRepository,
    private val universityRepository: UniversityRepository,
    private val movieRepository: EntertainmentMovieRepository,
    private val musicRepository: EntertainmentMusicRepository,
    private val riddleRepository: EntertainmentRiddleRepository,
    private val discountRepository: DiscountRepository,
    private val fieldOfStudyRepository: FieldOfStudyRepository,
    private val educationLevelRepository: EducationLevelRepository,
    private val facultyRepository: FacultyRepository
) {

    // 👤 User Management
    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<ApiResponse<List<UserDto>>> {
        val users = userRepository.findAll().map { it.toDto() }
        return ResponseEntity.ok(ApiResponse(true, "Success", users))
    }

    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        userRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "User deleted"))
    }

    // 🖼️ Banner Management
    @GetMapping("/banners")
    fun getBanners(): ResponseEntity<ApiResponse<List<HomeBanner>>> = 
        ResponseEntity.ok(ApiResponse(true, "Success", bannerRepository.findAll()))

    @PostMapping("/banners")
    fun createBanner(@RequestBody banner: HomeBanner): ResponseEntity<ApiResponse<HomeBanner>> =
        ResponseEntity.ok(ApiResponse(true, "Banner created", bannerRepository.save(banner)))

    @DeleteMapping("/banners/{id}")
    fun deleteBanner(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        bannerRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Banner deleted"))
    }

    // 🎓 University Management
    @GetMapping("/universities")
    fun getUniversities(): ResponseEntity<ApiResponse<List<University>>> = 
        ResponseEntity.ok(ApiResponse(true, "Success", universityRepository.findAll()))

    @PostMapping("/universities")
    fun createUniversity(@RequestBody university: University): ResponseEntity<ApiResponse<University>> {
        val existing = universityRepository.findByNameIgnoreCase(university.name.trim())
            .filter { it.id != university.id }
        if (existing.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse(false, "این دانشگاه قبلاً اضافه شده است"))
        }
        university.name = university.name.trim()
        return ResponseEntity.ok(ApiResponse(true, "University created", universityRepository.save(university)))
    }

    @DeleteMapping("/universities/{id}")
    fun deleteUniversity(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        universityRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "University deleted"))
    }

    // 📚 Field of Study Management
    @GetMapping("/fields-of-study")
    fun getFieldsOfStudy(): ResponseEntity<ApiResponse<List<FieldOfStudy>>> =
        ResponseEntity.ok(ApiResponse(true, "Success", fieldOfStudyRepository.findAllByOrderByDisplayOrderAsc()))

    @PostMapping("/fields-of-study")
    fun createFieldOfStudy(@RequestBody field: FieldOfStudy): ResponseEntity<ApiResponse<FieldOfStudy>> {
        if (field.educationLevel.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse(false, "تعیین مقطع تحصیلی الزامی است"))
        }
        val existing = fieldOfStudyRepository.findByNameIgnoreCaseAndEducationLevelIgnoreCase(
            field.name.trim(), field.educationLevel.trim()
        ).filter { it.id != field.id }
        if (existing.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse(false, "این رشته با این مقطع قبلاً اضافه شده است"))
        }
        field.name = field.name.trim()
        field.educationLevel = field.educationLevel.trim()
        return ResponseEntity.ok(ApiResponse(true, "Field created", fieldOfStudyRepository.save(field)))
    }

    @DeleteMapping("/fields-of-study/{id}")
    fun deleteFieldOfStudy(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        fieldOfStudyRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Field deleted"))
    }

    // 🎓 Education Level Management
    @GetMapping("/education-levels")
    fun getEducationLevels(): ResponseEntity<ApiResponse<List<EducationLevel>>> =
        ResponseEntity.ok(ApiResponse(true, "Success", educationLevelRepository.findAllByOrderByDisplayOrderAsc()))

    @PostMapping("/education-levels")
    fun createEducationLevel(@RequestBody level: EducationLevel): ResponseEntity<ApiResponse<EducationLevel>> =
        ResponseEntity.ok(ApiResponse(true, "Level created", educationLevelRepository.save(level)))

    @DeleteMapping("/education-levels/{id}")
    fun deleteEducationLevel(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        educationLevelRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Level deleted"))
    }

    // 🎬 Movie Management
    @GetMapping("/movies")
    fun getMovies(): ResponseEntity<ApiResponse<List<EntertainmentMovie>>> =
        ResponseEntity.ok(ApiResponse(true, "Success", movieRepository.findAll()))

    @PostMapping("/movies")
    fun createMovie(@RequestBody movie: EntertainmentMovie): ResponseEntity<ApiResponse<EntertainmentMovie>> =
        ResponseEntity.ok(ApiResponse(true, "Movie created", movieRepository.save(movie)))

    @DeleteMapping("/movies/{id}")
    fun deleteMovie(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        movieRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Movie deleted"))
    }

    // 🎵 Music Management
    @GetMapping("/music")
    fun getMusic(): ResponseEntity<ApiResponse<List<EntertainmentMusic>>> =
        ResponseEntity.ok(ApiResponse(true, "Success", musicRepository.findAll()))

    @PostMapping("/music")
    fun createMusic(@RequestBody music: EntertainmentMusic): ResponseEntity<ApiResponse<EntertainmentMusic>> =
        ResponseEntity.ok(ApiResponse(true, "Music created", musicRepository.save(music)))

    @DeleteMapping("/music/{id}")
    fun deleteMusic(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        musicRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Music deleted"))
    }

    // 🧩 Riddle Management
    @GetMapping("/riddles")
    fun getRiddles(): ResponseEntity<ApiResponse<List<EntertainmentRiddle>>> =
        ResponseEntity.ok(ApiResponse(true, "Success", riddleRepository.findAll()))

    @PostMapping("/riddles")
    fun createRiddle(@RequestBody riddle: EntertainmentRiddle): ResponseEntity<ApiResponse<EntertainmentRiddle>> =
        ResponseEntity.ok(ApiResponse(true, "Riddle created", riddleRepository.save(riddle)))

    @DeleteMapping("/riddles/{id}")
    fun deleteRiddle(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        riddleRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Riddle deleted"))
    }

    // 💸 Discount Management
    @GetMapping("/discounts")
    fun getDiscounts(): ResponseEntity<ApiResponse<List<Discount>>> =
        ResponseEntity.ok(ApiResponse(true, "Success", discountRepository.findAll()))

    @PostMapping("/discounts")
    fun createDiscount(@RequestBody discount: Discount): ResponseEntity<ApiResponse<Discount>> =
        ResponseEntity.ok(ApiResponse(true, "Discount created", discountRepository.save(discount)))

    @DeleteMapping("/discounts/{id}")
    fun deleteDiscount(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        discountRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Discount deleted"))
    }

    // 🏛️ Faculty Management
    @GetMapping("/faculties")
    fun getFaculties(): ResponseEntity<ApiResponse<List<Faculty>>> =
        ResponseEntity.ok(ApiResponse(true, "Success", facultyRepository.findAllByOrderByDisplayOrderAsc()))

    @PostMapping("/faculties")
    fun createFaculty(@RequestBody faculty: Faculty): ResponseEntity<ApiResponse<Faculty>> {
        val existing = facultyRepository.findByNameIgnoreCase(faculty.name.trim())
            .filter { it.id != faculty.id }
        if (existing.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse(false, "این دانشکده قبلاً اضافه شده است"))
        }
        faculty.name = faculty.name.trim()
        return ResponseEntity.ok(ApiResponse(true, "Faculty created", facultyRepository.save(faculty)))
    }

    @DeleteMapping("/faculties/{id}")
    fun deleteFaculty(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        facultyRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Faculty deleted"))
    }
}

