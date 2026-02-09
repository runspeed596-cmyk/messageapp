package com.Kelasor.app.data.remote.api

import com.Kelasor.app.data.remote.dto.ApiResponse
import com.Kelasor.app.data.remote.dto.EntertainmentResponse
import com.Kelasor.app.data.remote.dto.RiddleResultDto
import com.Kelasor.app.data.remote.dto.SolveRiddleRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EntertainmentApiService {
    @GET("api/entertainment")
    suspend fun getEntertainment(): ApiResponse<EntertainmentResponse>

    @POST("api/entertainment/solve")
    suspend fun solveRiddle(@Body request: SolveRiddleRequest): ApiResponse<RiddleResultDto>
}
