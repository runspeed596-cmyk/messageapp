package com.Kelasor.app.data.repository

import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.InstitutionRegisterRequestDto
import com.Kelasor.app.domain.mapper.toDomain
import com.Kelasor.app.domain.model.Institution
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstitutionRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getInstitution(id: String): Institution? {
        return try {
            val response = apiService.getInstitution(id)
            if (response.isSuccessful) {
                response.body()?.data?.toDomain()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun uploadLogo(file: java.io.File): Result<String> {
        return try {
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val response = apiService.uploadInstitutionLogo(part)
            if (response.isSuccessful && response.body()?.success == true) {
                val logoUrl: String = response.body()?.data ?: return Result.failure(Exception("Empty response"))
                Result.success(logoUrl)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Logo upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerInstitution(request: InstitutionRegisterRequestDto): Result<Institution> {
        return try {
            val response = apiService.registerInstitution(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()?.data ?: return Result.failure(Exception("Empty response body"))
                Result.success(dto.toDomain())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInstitution(id: String, request: InstitutionRegisterRequestDto): Result<Institution> {
        return try {
            val response = apiService.updateInstitution(id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()?.data ?: return Result.failure(Exception("Empty response body"))
                Result.success(dto.toDomain())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHonors(id: String): List<com.Kelasor.app.data.remote.dto.InstitutionHonorDto> {
        return try {
            val response = apiService.getInstitutionHonors(id)
            if (response.isSuccessful) response.body()?.data ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTeachers(id: String): List<com.Kelasor.app.domain.model.User> {
        return try {
            val response = apiService.getInstitutionTeachers(id)
            if (response.isSuccessful) response.body()?.data?.map { it.toDomain() } ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAdmins(id: String): List<com.Kelasor.app.domain.model.User> {
        return try {
            val response = apiService.getInstitutionAdmins(id)
            if (response.isSuccessful) response.body()?.data?.map { it.toDomain() } ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addHonor(id: String, honor: com.Kelasor.app.data.remote.dto.InstitutionHonorDto): Result<com.Kelasor.app.data.remote.dto.InstitutionHonorDto> {
        return try {
            val response = apiService.addInstitutionHonor(id, honor)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to add honor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getActiveInstitutions(page: Int = 0, size: Int = 50): Result<List<Institution>> {
        return try {
            val response = apiService.getActiveInstitutions(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val dtoList = response.body()?.data?.content ?: emptyList()
                Result.success(dtoList.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch active institutions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
