package com.Kelasor.app.data.repository

import com.Kelasor.app.data.remote.api.PollApiService
import com.Kelasor.app.data.remote.dto.CreatePollRequest
import com.Kelasor.app.data.remote.dto.PollDto
import com.Kelasor.app.data.remote.dto.VoteRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PollRepository @Inject constructor(
    private val api: PollApiService
) {
    suspend fun createPoll(question: String, options: List<String>, isMultipleChoice: Boolean, isAnonymous: Boolean): Result<PollDto> {
        return try {
            android.util.Log.d("PollRepository", "Creating poll: question='$question', options=${options.size}, multiple=$isMultipleChoice, anon=$isAnonymous")
            val request = CreatePollRequest(question, options, isMultipleChoice, isAnonymous)
            val httpResponse = api.createPoll(request)
            
            android.util.Log.d("PollRepository", "HTTP Response: code=${httpResponse.code()}, isSuccessful=${httpResponse.isSuccessful}")
            
            if (httpResponse.isSuccessful) {
                val response = httpResponse.body()
                android.util.Log.d("PollRepository", "Response body: success=${response?.success}, message='${response?.message}', hasData=${response?.data != null}")
                
                if (response?.data != null) {
                    // Server returned data (either wrapped or unwrapped)
                    val pollDto = response.data
                    android.util.Log.d("PollRepository", "Poll data: id=${pollDto.id}, question=${pollDto.question}, options.size=${pollDto.options.size}")
                    pollDto.options.forEachIndexed { i, opt ->
                        android.util.Log.d("PollRepository", "  Option $i: id=${opt.id}, text=${opt.text}, voteCount=${opt.voteCount}")
                    }
                    
                    // CRITICAL FIX: Backend returns pollDto without options populated
                    // Fetch the full poll data to get real option IDs
                    val finalPollDto = if (pollDto.options.isEmpty()) {
                        android.util.Log.d("PollRepository", "Backend returned empty options, fetching full poll data...")
                        try {
                            val fullPollResponse = api.getPoll(pollDto.id)
                            if (fullPollResponse.success && fullPollResponse.data != null) {
                                android.util.Log.d("PollRepository", "Fetched full poll: options.size=${fullPollResponse.data.options.size}")
                                fullPollResponse.data.options.forEachIndexed { i, opt ->
                                    android.util.Log.d("PollRepository", "  Real option $i: id=${opt.id}, text=${opt.text}")
                                }
                                fullPollResponse.data
                            } else {
                                // Fallback: manually construct from input if fetch fails
                                android.util.Log.d("PollRepository", "Failed to fetch, using manual construction")
                                pollDto.copy(
                                    options = options.mapIndexed { index, text ->
                                        com.Kelasor.app.data.remote.dto.PollOptionDto(
                                            id = "${pollDto.id}_option_$index",
                                            text = text,
                                            voteCount = 0,
                                            votePercentage = 0f
                                        )
                                    }
                                )
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("PollRepository", "Error fetching full poll", e)
                            // Fallback: manually construct
                            pollDto.copy(
                                options = options.mapIndexed { index, text ->
                                    com.Kelasor.app.data.remote.dto.PollOptionDto(
                                        id = "${pollDto.id}_option_$index",
                                        text = text,
                                        voteCount = 0,
                                        votePercentage = 0f
                                    )
                                }
                            )
                        }
                    } else {
                        pollDto
                    }
                    
                    android.util.Log.d("PollRepository", "Final poll options.size=${finalPollDto.options.size}")
                    Result.success(finalPollDto)
                } else if (response?.success == true) {
                    // If success but no data, something is wrong
                    Result.failure(Exception("نظرسنجی ایجاد شد اما داده‌ای دریافت نشد"))
                } else {
                    val errorMsg = response?.message ?: "خطای ناشناخته از سرور"
                    android.util.Log.e("PollRepository", "Poll creation failed: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = httpResponse.errorBody()?.string()
                android.util.Log.e("PollRepository", "HTTP error: ${httpResponse.code()} - $errorBody")
                Result.failure(Exception("خطای سرور: ${httpResponse.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("PollRepository", "Exception creating poll: ${e.javaClass.simpleName} - ${e.message}", e)
            val errorMsg = when(e) {
                is java.net.ConnectException -> "امکان اتصال به سرور نیست. اتصال اینترنت خود را بررسی کنید."
                is java.net.SocketTimeoutException -> "زمان اتصال به سرور به پایان رسید. دوباره تلاش کنید."
                is com.google.gson.JsonSyntaxException -> "پاسخ نامعتبر از سرور: ${e.message}"
                else -> "خطا: ${e.message ?: e.javaClass.simpleName}"
            }
            Result.failure(Exception(errorMsg, e))
        }
    }

    suspend fun vote(pollId: String, optionIds: List<String>): Result<PollDto> {
        return try {
            android.util.Log.d("PollRepository", "Voting on poll: $pollId with options: $optionIds")
            val request = VoteRequest(optionIds)
            val response = api.vote(pollId, request)
            android.util.Log.d("PollRepository", "Vote response: success=${response.success}, message=${response.message}, hasData=${response.data != null}")
            if (response.success && response.data != null) {
                android.util.Log.d("PollRepository", "Vote successful!")
                Result.success(response.data)
            } else {
                android.util.Log.e("PollRepository", "Vote failed: ${response.message}")
                Result.failure(Exception(response.message ?: "خطا در ثبت رأی"))
            }
        } catch (e: Exception) {
            android.util.Log.e("PollRepository", "Vote exception: ${e.javaClass.simpleName} - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun retractVote(pollId: String): Result<PollDto> {
        return try {
            val response = api.retractVote(pollId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPoll(pollId: String): Result<PollDto> {
        return try {
            android.util.Log.d("PollRepository", "Fetching poll: $pollId")
            val response = api.getPoll(pollId)
            if (response.success && response.data != null) {
                android.util.Log.d("PollRepository", "Poll fetched successfully: ${response.data.options.size} options")
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch poll"))
            }
        } catch (e: Exception) {
            android.util.Log.e("PollRepository", "Error fetching poll", e)
            Result.failure(e)
        }
    }
}
