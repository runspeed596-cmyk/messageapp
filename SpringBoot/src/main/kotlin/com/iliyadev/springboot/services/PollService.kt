package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PollService(
    private val pollRepository: PollRepository,
    private val pollOptionRepository: PollOptionRepository,
    private val pollVoteRepository: PollVoteRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createPoll(userId: UUID, request: CreatePollRequest): Poll {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        
        val poll = Poll(
            question = request.question,
            isMultipleChoice = request.isMultipleChoice,
            isAnonymous = request.isAnonymous,
            creator = user
        )
        
        val savedPoll = pollRepository.save(poll)
        
        request.options.forEach { optionText ->
            pollOptionRepository.save(
                PollOption(
                    poll = savedPoll,
                    text = optionText
                )
            )
        }
        
        return savedPoll
    }

    @Transactional
    fun vote(userId: UUID, pollId: UUID, optionIds: List<UUID>): Poll {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        val poll = pollRepository.findById(pollId).orElseThrow { IllegalArgumentException("Poll not found") }
        
        if (!poll.isMultipleChoice && optionIds.size > 1) {
            throw IllegalArgumentException("This poll does not allow multiple choices")
        }

        // Remove existing votes for this user
        // We need to fetch them first to decrement counts or we handle counts dynamically?
        // PollOption has 'voteCount'. We should maintain it for performance.
        val existingVotes = pollVoteRepository.findAllByPollAndUser(poll, user)
        existingVotes.forEach { vote ->
            val option = vote.option
            if (option != null) {
                option.voteCount = (option.voteCount - 1).coerceAtLeast(0)
                pollOptionRepository.save(option)
            }
        }
        pollVoteRepository.deleteByPollAndUser(poll, user)
        
        // Add new votes
        val freshOptions = pollOptionRepository.findAllById(optionIds)
        freshOptions.forEach { option ->
            if (option.poll?.id != pollId) {
                throw IllegalArgumentException("Option ${option.id} does not belong to poll $pollId")
            }
            
            pollVoteRepository.save(
                PollVote(
                    poll = poll,
                    option = option,
                    user = user,
                    votedAt = Instant.now()
                )
            )
            
            option.voteCount += 1
            pollOptionRepository.save(option)
        }
        
        // Return updated poll
        return pollRepository.findById(pollId).get()
    }
    
    @Transactional
    fun retractVote(userId: UUID, pollId: UUID): Poll {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        val poll = pollRepository.findById(pollId).orElseThrow { IllegalArgumentException("Poll not found") }
        
        val existingVotes = pollVoteRepository.findAllByPollAndUser(poll, user)
        existingVotes.forEach { vote ->
            val option = vote.option
            if (option != null) {
                option.voteCount = (option.voteCount - 1).coerceAtLeast(0)
                pollOptionRepository.save(option)
            }
        }
        pollVoteRepository.deleteByPollAndUser(poll, user)
        
        return pollRepository.findById(pollId).get()
    }

    @Transactional(readOnly = true)
    fun getPoll(pollId: UUID): Poll {
         return pollRepository.findById(pollId).orElseThrow { IllegalArgumentException("Poll not found") }
    }
}
