package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import com.iliyadev.springboot.services.*
import org.springframework.data.domain.PageRequest
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
    private val facultyRepository: FacultyRepository,
    private val educationalRoleOptionRepository: EducationalRoleOptionRepository,
    private val panelAdminService: PanelAdminService,
    private val clubRepository: ClubRepository,
    private val studentOrgRepository: StudentOrgRepository
) {

    // 👤 User Management
    @GetMapping("/users")
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val pageable = PageRequest.of(page, size)
        val pageResult = userRepository.findAll(pageable)
        val users = pageResult.content.map { it.toDto() }
        val data = mapOf(
            "content" to users,
            "totalElements" to pageResult.totalElements,
            "totalPages" to pageResult.totalPages,
            "number" to pageResult.number,
            "size" to pageResult.size
        )
        return ResponseEntity.ok(ApiResponse(true, "Success", data))
    }

    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserDto>> {
        val user = userRepository.findById(id).orElse(null)
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse(false, "کاربر یافت نشد"))
        }
        return ResponseEntity.ok(ApiResponse(true, "Success", user.toDto()))
    }

    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        userRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "User deleted"))
    }

    // 🛡️ Panel Admin Management
    @GetMapping("/panel-admins")
    fun getPanelAdmins(): ResponseEntity<ApiResponse<List<PanelAdminDto>>> {
        val admins = panelAdminService.fetchAllAdmins().map { PanelAdminDto(
            id = it.id.toString(),
            username = it.username,
            displayName = it.displayName,
            isSuperAdmin = it.isSuperAdmin,
            permissions = it.permissions,
            createdAt = it.createdAt.toString()
        )}
        return ResponseEntity.ok(ApiResponse(true, "Success", admins))
    }

    @PostMapping("/panel-admins")
    fun createPanelAdmin(@RequestBody request: CreatePanelAdminRequest): ResponseEntity<ApiResponse<PanelAdminDto>> {
        return try {
            val admin = panelAdminService.createAdmin(
                username = request.username,
                password = request.password,
                displayName = request.displayName,
                isSuperAdmin = request.isSuperAdmin,
                permissions = request.permissions
            )
            val dto = PanelAdminDto(
                id = admin.id.toString(),
                username = admin.username,
                displayName = admin.displayName,
                isSuperAdmin = admin.isSuperAdmin,
                permissions = admin.permissions,
                createdAt = admin.createdAt.toString()
            )
            ResponseEntity.ok(ApiResponse(true, "ادمین با موفقیت ایجاد شد", dto))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse(false, e.message ?: "خطا"))
        }
    }

    @DeleteMapping("/panel-admins/{id}")
    fun deletePanelAdmin(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        return try {
            panelAdminService.deleteAdmin(id)
            ResponseEntity.ok(ApiResponse(true, "ادمین با موفقیت حذف شد"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse(false, e.message ?: "خطا"))
        }
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
    fun getUniversities(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val pageable = PageRequest.of(page, size)
        val pageResult = universityRepository.findAll(pageable)
        val data = mapOf(
            "content" to pageResult.content,
            "totalElements" to pageResult.totalElements,
            "totalPages" to pageResult.totalPages,
            "number" to pageResult.number,
            "size" to pageResult.size
        )
        return ResponseEntity.ok(ApiResponse(true, "Success", data))
    }

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
        ResponseEntity.ok(ApiResponse(true, "Success", fieldOfStudyRepository.findAll()))

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
        ResponseEntity.ok(ApiResponse(true, "Success", educationLevelRepository.findAll()))

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
        ResponseEntity.ok(ApiResponse(true, "Success", facultyRepository.findAll()))

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

    // 🎭 Educational Role Management
    @GetMapping("/educational-roles")
    fun getEducationalRoles(): ResponseEntity<ApiResponse<List<EducationalRoleOption>>> =
        ResponseEntity.ok(ApiResponse(true, "Success", educationalRoleOptionRepository.findAll()))

    @PostMapping("/educational-roles")
    fun createEducationalRole(@RequestBody role: EducationalRoleOption): ResponseEntity<ApiResponse<EducationalRoleOption>> =
        ResponseEntity.ok(ApiResponse(true, "Role created", educationalRoleOptionRepository.save(role)))

    @DeleteMapping("/educational-roles/{id}")
    fun deleteEducationalRole(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        educationalRoleOptionRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Role deleted"))
    }

    // 🏛️ Club Management (کانون‌ها)
    @GetMapping("/clubs")
    fun getClubs(): ResponseEntity<ApiResponse<List<Club>>> =
        ResponseEntity.ok(ApiResponse(true, "Success", clubRepository.findAll()))

    @PostMapping("/clubs")
    fun createClub(@RequestBody club: Club): ResponseEntity<ApiResponse<Club>> =
        ResponseEntity.ok(ApiResponse(true, "Club created", clubRepository.save(club)))

    @DeleteMapping("/clubs/{id}")
    fun deleteClub(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        clubRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Club deleted"))
    }

    // 🎓 Student Organization Management (تشکل‌های دانشجویی)
    @GetMapping("/student-orgs")
    fun getStudentOrgs(): ResponseEntity<ApiResponse<List<StudentOrg>>> =
        ResponseEntity.ok(ApiResponse(true, "Success", studentOrgRepository.findAll()))

    @PostMapping("/student-orgs")
    fun createStudentOrg(@RequestBody org: StudentOrg): ResponseEntity<ApiResponse<StudentOrg>> =
        ResponseEntity.ok(ApiResponse(true, "Student org created", studentOrgRepository.save(org)))

    @DeleteMapping("/student-orgs/{id}")
    fun deleteStudentOrg(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        studentOrgRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Student org deleted"))
    }

    // 🖼️ Mosbat Elm Banner Management (اسلایدر مثبت علم)
    @GetMapping("/mosbat-elm/banners")
    fun getMosbatElmBanners(): ResponseEntity<ApiResponse<List<HomeBanner>>> =
        ResponseEntity.ok(ApiResponse(true, "Success", bannerRepository.findAllBySectionAndIsActiveTrueOrderByCreatedAtDesc("MOSBAT_ELM")))

    @PostMapping("/mosbat-elm/banners")
    fun createMosbatElmBanner(@RequestBody banner: HomeBanner): ResponseEntity<ApiResponse<HomeBanner>> {
        banner.section = "MOSBAT_ELM"
        return ResponseEntity.ok(ApiResponse(true, "Banner created", bannerRepository.save(banner)))
    }

    @DeleteMapping("/mosbat-elm/banners/{id}")
    fun deleteMosbatElmBanner(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        bannerRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "Banner deleted"))
    }
}

// DTOs for Panel Admin
data class PanelAdminDto(
    val id: String,
    val username: String,
    val displayName: String,
    val isSuperAdmin: Boolean,
    val permissions: List<String>,
    val createdAt: String
)

data class CreatePanelAdminRequest(
    val username: String,
    val password: String,
    val displayName: String,
    val isSuperAdmin: Boolean = false,
    val permissions: List<String> = emptyList()
)

