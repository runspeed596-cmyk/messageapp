package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EntertainmentService(
    private val movieRepository: EntertainmentMovieRepository,
    private val musicRepository: EntertainmentMusicRepository,
    private val riddleRepository: EntertainmentRiddleRepository,
    private val userRepository: UserRepository
) {

    fun getEntertainmentData(): EntertainmentResponse {
        val movies = movieRepository.findAllByIsActiveTrue().map { movie ->
            MovieDto(
                id = movie.id ?: UUID.randomUUID(),
                title = movie.title,
                description = movie.description ?: "",
                videoUrl = movie.videoUrl,
                thumbnailUrl = movie.thumbnailUrl,
                duration = movie.duration ?: "",
                releaseDate = movie.releaseDate ?: ""
            )
        }

        val music = musicRepository.findAllByIsActiveTrue().map { track ->
            MusicDto(
                id = track.id ?: UUID.randomUUID(),
                title = track.title,
                artist = track.artist ?: "Unknown",
                audioUrl = track.audioUrl,
                coverUrl = track.coverUrl,
                duration = track.duration ?: ""
            )
        }

        val challenges = riddleRepository.findAllByIsActiveTrue().map { riddle ->
            GameChallengeDto(
                id = riddle.id ?: UUID.randomUUID(),
                title = riddle.title,
                description = riddle.description ?: "",
                question = riddle.question,
                reward = riddle.reward ?: "",
                type = riddle.type,
                isMultipleChoice = riddle.isMultipleChoice,
                options = riddle.options.map { option ->
                    RiddleOptionDto(
                        id = option.id ?: UUID.randomUUID(),
                        text = option.text,
                        displayOrder = option.displayOrder
                    )
                }.sortedBy { it.displayOrder },
                correctAnswerIndex = riddle.correctAnswerIndex
            )
        }

        return EntertainmentResponse(
            movies = movies,
            music = music,
            challenges = challenges
        )
    }

    fun solveRiddle(userId: UUID, riddleId: UUID, answerIndex: Int): RiddleResult {
        val riddle = riddleRepository.findById(riddleId).orElseThrow { Exception("Riddle not found") }
        val user = userRepository.findById(userId).orElseThrow { Exception("User not found") }

        if (riddle.correctAnswerIndex == answerIndex) {
            // Award points (assuming reward string like "10" or "10 points")
            val rewardAmount = riddle.reward?.filter { it.isDigit() }?.toLongOrNull() ?: 10L
            user.points += rewardAmount
            userRepository.save(user)
            
            return RiddleResult(true, "تبریک! پاسخ صحیح بود. $rewardAmount امتیاز دریافت کردید.", rewardAmount)
        } else {
            return RiddleResult(false, "متاسفانه پاسخ اشتباه بود. دوباره تلاش کنید.", 0)
        }
    }
}

