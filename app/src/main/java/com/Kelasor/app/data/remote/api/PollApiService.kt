package com.Kelasor.app.data.remote.api

import com.Kelasor.app.data.remote.dto.ApiResponse
import com.Kelasor.app.data.remote.dto.CreatePollRequest
import com.Kelasor.app.data.remote.dto.PollDto
import com.Kelasor.app.data.remote.dto.VoteRequest
import retrofit2.http.*

interface PollApiService {
    @POST("api/polls")
    suspend fun createPoll(@Body request: CreatePollRequest): retrofit2.Response<ApiResponse<PollDto>>

    @POST("api/polls/{pollId}/vote")
    suspend fun vote(@Path("pollId") pollId: String, @Body request: VoteRequest): ApiResponse<PollDto>

    @DELETE("api/polls/{pollId}/vote")
    suspend fun retractVote(@Path("pollId") pollId: String): ApiResponse<PollDto>

    @GET("api/polls/{pollId}")
    suspend fun getPoll(@Path("pollId") pollId: String): ApiResponse<PollDto>
}
