package com.Kelasor.app.data.repository

import com.Kelasor.app.data.local.dao.CourseDao
import com.Kelasor.app.data.local.entity.CourseEntity
import com.Kelasor.app.data.remote.dto.CourseDto
import com.Kelasor.app.data.remote.dto.EnrollmentResponseDto
import com.Kelasor.app.data.session.SessionManager
import com.Kelasor.app.domain.mapper.toDomain
import com.Kelasor.app.domain.model.Course
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val courseDao: CourseDao,
    private val apiService: com.Kelasor.app.data.remote.api.ApiService,
    private val channelRepository: ChannelRepository,
    private val sessionManager: SessionManager
) {
    // ── Remote API Methods ──

    suspend fun getCourseById(courseId: String): Result<com.Kelasor.app.domain.model.Course> {
        return try {
            val response = apiService.getCourseById(courseId)
            if (response.isSuccessful && response.body()?.success == true) {
                val courseDto = response.body()?.data
                if (courseDto != null) {
                    Result.success(courseDto.toDomain())
                } else {
                    Result.failure(Exception("Course data is null"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Course not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJoinClassUrl(courseId: String): Result<String> {
        return try {
            val response = apiService.getJoinClassUrl(courseId)
            if (response.isSuccessful && response.body()?.success == true) {
                val url = response.body()?.data?.get("url")
                if (url != null) Result.success(url) else Result.failure(Exception("URL is null"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get join URL"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createKelasorOnline(courseId: String): Result<com.Kelasor.app.domain.model.Course> {
        return try {
            val response = apiService.createKelasorOnline(courseId)
            if (response.isSuccessful && response.body()?.success == true) {
                val courseDto = response.body()?.data
                if (courseDto != null) {
                    Result.success(courseDto.toDomain())
                } else {
                    Result.failure(Exception("Course data is null"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create kelasor online"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCoursesByInstitution(institutionId: String, page: Int, size: Int): List<com.Kelasor.app.domain.model.Course> {
        return try {
            val response = apiService.getInstitutionCourses(institutionId, page, size)
            if (response.isSuccessful) {
                response.body()?.data?.content?.map { it.toDomain() } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSimilarCourses(courseId: String): List<com.Kelasor.app.domain.model.Course> {
        return try {
            val response = apiService.getSimilarCourses(courseId)
            if (response.isSuccessful) {
                response.body()?.data?.content?.map { it.toDomain() } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPublicCourses(page: Int = 0, size: Int = 20): List<com.Kelasor.app.domain.model.Course> {
        return try {
            val response = apiService.getPublicCourses(page, size)
            if (response.isSuccessful) {
                response.body()?.data?.content?.map { it.toDomain() } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun toggleFavorite(courseId: String): Result<Boolean> {
        return try {
            val response = apiService.toggleCourseFavorite(courseId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: false)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Toggle favorite failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isFavorite(courseId: String): Boolean {
        return try {
            val response = apiService.isCourseFavorite(courseId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getFavoriteCourses(): List<CourseDto> {
        return try {
            val response = apiService.getFavoriteCourses()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun enrollInCourse(courseId: String, paymentType: String): Result<Unit> {
        return try {
            val response = apiService.enrollInCourse(courseId, com.Kelasor.app.data.remote.dto.EnrollmentRequestDto(paymentType))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Enrollment failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isEnrolled(courseId: String): Boolean {
        return try {
            val response = apiService.isEnrolledInCourse(courseId)
            if (response.isSuccessful) response.body()?.data ?: false else false
        } catch (e: Exception) {
            false
        }
    }

    // ── Local DB ──

    fun observeAllCourses(): Flow<List<CourseEntity>> = courseDao.observeAllCourses()

    // ── Course Creation (API-backed) ──

    suspend fun registerCourse(request: com.Kelasor.app.data.remote.dto.CreateCourseRequest): Result<com.Kelasor.app.domain.model.Course> {
        return try {
            val response = apiService.createCourse(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val courseDto = response.body()?.data
                if (courseDto != null) {
                    Result.success(courseDto.toDomain())
                } else {
                    Result.failure(Exception("Course data is null"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create course"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCourse(courseId: String, request: com.Kelasor.app.data.remote.dto.CreateCourseRequest): Result<com.Kelasor.app.domain.model.Course> {
        return try {
            val response = apiService.updateCourse(courseId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val courseDto = response.body()?.data
                if (courseDto != null) {
                    Result.success(courseDto.toDomain())
                } else {
                    Result.failure(Exception("Course data is null"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to update course"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Course Review (Admin) ──

    suspend fun reviewCourse(courseId: String, adminId: String, status: String, adminNote: String?): Result<com.Kelasor.app.domain.model.Course> {
        return try {
            val request = com.Kelasor.app.data.remote.dto.CourseReviewRequestDto(status, adminNote)
            val response = apiService.reviewCourse(courseId, adminId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val courseDto = response.body()?.data
                if (courseDto != null) {
                    Result.success(courseDto.toDomain())
                } else {
                    Result.failure(Exception("Course data is null"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to review course"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Comments ──

    suspend fun getComments(courseId: String, page: Int = 0, size: Int = 20): List<com.Kelasor.app.data.remote.dto.CourseCommentDto> {
        return try {
            val response = apiService.getCourseComments(courseId, page, size)
            if (response.isSuccessful) {
                response.body()?.data?.content ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addComment(courseId: String, content: String, rating: Int, replyToId: String? = null): Result<com.Kelasor.app.data.remote.dto.CourseCommentDto> {
        return try {
            val request = com.Kelasor.app.data.remote.dto.AddCourseCommentRequest(content, rating, replyToId)
            val response = apiService.addCourseComment(courseId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()?.data
                if (dto != null) Result.success(dto) else Result.failure(Exception("Comment data is null"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to add comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Collaboration ──

    suspend fun getPendingCollaborations(academyId: String, page: Int = 0, size: Int = 20): Result<List<com.Kelasor.app.data.remote.dto.CourseCollaborationRequestDto>> {
        return try {
            val response = apiService.getPendingCollaborations(academyId, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.content ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load collaborations"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestCollaboration(courseId: String, targetInstitutionId: String, message: String?): Result<com.Kelasor.app.data.remote.dto.CourseCollaborationRequestDto> {
        return try {
            val request = com.Kelasor.app.data.remote.dto.CreateCollaborationRequest(targetInstitutionId, message)
            val response = apiService.requestCollaboration(courseId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()?.data
                if (dto != null) Result.success(dto) else Result.failure(Exception("Data is null"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to request collaboration"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptCollaboration(requestId: String): Result<Unit> {
        return try {
            val response = apiService.acceptCollaboration(requestId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to accept collaboration"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectCollaboration(requestId: String): Result<Unit> {
        return try {
            val response = apiService.rejectCollaboration(requestId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to reject collaboration"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Channels by User ──

    suspend fun getChannelsByUser(userId: String): List<com.Kelasor.app.data.remote.dto.ChannelDto> {
        return try {
            val response = apiService.getChannelsByUser(userId)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getInstitution(id: String): Result<com.Kelasor.app.domain.model.Institution> {
        return try {
            val response = apiService.getInstitution(id)
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()?.data
                if (dto != null) {
                    Result.success(dto.toDomain())
                } else {
                    Result.failure(Exception("Institution data is null"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Institution not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCourse(courseId: String): Result<Unit> {
        return try {
            val response = apiService.deleteCourse(courseId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete course"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyCourses(): List<com.Kelasor.app.domain.model.Course> {
        return try {
            val response = apiService.getMyCourses()
            if (response.isSuccessful) {
                response.body()?.data?.content?.map { it.toDomain() } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMyEnrollments(): List<Course> {
        try {
            val response = apiService.getMyEnrollments()
            if (response.isSuccessful) {
                val data = response.body()?.data
                val content = data?.content
                return content?.mapNotNull { it.course?.toDomain() } ?: emptyList()
            }
            return emptyList()
        } catch (e: Exception) {
            return emptyList()
        }
    }

    suspend fun uploadFile(uri: android.net.Uri, context: android.content.Context): Result<String> {
        return try {
            val file = java.io.File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            val response = apiService.uploadFile(body)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: "")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPoster(file: java.io.File): Result<String> {
        return try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = apiService.uploadFile(body)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: "")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
