package com.iliyadev.springboot.repositories

import com.iliyadev.springboot.models.Poll
import com.iliyadev.springboot.models.PollOption
import com.iliyadev.springboot.models.PollVote
import com.iliyadev.springboot.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PollRepository : JpaRepository<Poll, UUID>

@Repository
interface PollOptionRepository : JpaRepository<PollOption, UUID>

@Repository
interface PollVoteRepository : JpaRepository<PollVote, UUID> {
    fun findAllByPollAndUser(poll: Poll, user: User): List<PollVote>
    fun deleteByPollAndUser(poll: Poll, user: User)
    fun countByPoll(poll: Poll): Int
    fun countByOption(option: PollOption): Int
}
