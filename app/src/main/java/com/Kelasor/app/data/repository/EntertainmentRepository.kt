package com.Kelasor.app.data.repository

import com.Kelasor.app.data.remote.api.EntertainmentApiService
import com.Kelasor.app.data.remote.dto.EntertainmentResponse
import com.Kelasor.app.data.remote.dto.RiddleResultDto
import com.Kelasor.app.data.remote.dto.SolveRiddleRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntertainmentRepository @Inject constructor(
    private val api: EntertainmentApiService
) {
    suspend fun getEntertainmentData(): Result<EntertainmentResponse> {
        return try {
            val response = api.getEntertainment()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun solveRiddle(riddleId: String, answerIndex: Int): Result<RiddleResultDto> {
        return try {
            val response = api.solveRiddle(SolveRiddleRequest(riddleId, answerIndex))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
