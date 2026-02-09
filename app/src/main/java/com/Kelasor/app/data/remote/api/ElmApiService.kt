package com.Kelasor.app.data.remote.api

import com.Kelasor.app.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ElmApiService {
    @GET("api/elm-peak/events")
    suspend fun getElmPeakData(): ElmPeakResponse

    @POST("api/elm-peak/ideas")
    suspend fun submitIdea(@Body request: IdeaSubmissionRequest): Map<String, String>

    @POST("api/elm-peak/reports")
    suspend fun reportEvent(@Body request: EventReportRequest): Map<String, String>

    @GET("api/elm-peak/universities")
    suspend fun getUniversities(): List<UniversityDto>
}
