package com.Kelasor.app.data.repository

import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.ReferenceDataDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class ReferenceDataResult {
    data object Loading : ReferenceDataResult()
    data class Success(val data: ReferenceDataDto) : ReferenceDataResult()
    data class Error(val message: String) : ReferenceDataResult()
}

@Singleton
class ReferenceDataRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun fetchReferenceData(): Flow<ReferenceDataResult> = flow {
        emit(ReferenceDataResult.Loading)
        try {
            val response = apiService.getReferenceData()
            if (response.isSuccessful) {
                val data: ReferenceDataDto = response.body()?.data ?: ReferenceDataDto()
                emit(ReferenceDataResult.Success(data))
            } else {
                emit(ReferenceDataResult.Error("خطا در دریافت اطلاعات"))
            }
        } catch (e: Exception) {
            emit(ReferenceDataResult.Error(e.message ?: "خطای شبکه"))
        }
    }
}
