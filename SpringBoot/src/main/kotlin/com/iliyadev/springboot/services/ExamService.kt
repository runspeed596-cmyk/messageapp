package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// Request / Response DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class CreateExamRequest(
    val title: String,
    val description: String? = null,
    val courseId: UUID? = null,
    val channelId: UUID? = null,
    val startsAt: Instant,
    val endsAt: Instant,
    val durationMinutes: Int = 60,
    val totalScore: BigDecimal = BigDecimal.ZERO,
    val passScore: BigDecimal? = null,
    val isPublic: Boolean = false,
    val shuffleQuestions: Boolean = false,
    val shuffleOptions: Boolean = false,
    val showResultsAfter: Boolean = true,
    val maxAttempts: Int = 1
)

data class UpdateExamRequest(
    val title: String? = null,
    val description: String? = null,
    val startsAt: Instant? = null,
    val endsAt: Instant? = null,
    val durationMinutes: Int? = null,
    val totalScore: BigDecimal? = null,
    val passScore: BigDecimal? = null,
    val isPublic: Boolean? = null,
    val shuffleQuestions: Boolean? = null,
    val shuffleOptions: Boolean? = null,
    val showResultsAfter: Boolean? = null,
    val maxAttempts: Int? = null
)

data class ExamResponse(
    val id: UUID,
    val title: String,
    val description: String?,
    val creatorId: UUID,
    val courseId: UUID?,
    val channelId: UUID?,
    val startsAt: Instant,
    val endsAt: Instant,
    val durationMinutes: Int,
    val totalScore: BigDecimal,
    val passScore: BigDecimal?,
    val status: ExamStatus,
    val isPublic: Boolean,
    val shuffleQuestions: Boolean,
    val shuffleOptions: Boolean,
    val showResultsAfter: Boolean,
    val maxAttempts: Int,
    val questionCount: Long,
    val attemptCount: Long,
    val createdAt: Instant
)

data class AddQuestionRequest(
    val questionType: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val questionText: String,
    val imageUrl: String? = null,
    val points: BigDecimal = BigDecimal.ONE,
    val sortOrder: Int = 0,
    val correctAnswer: String? = null,
    val options: List<QuestionOptionRequest> = emptyList()
)

data class QuestionOptionRequest(
    val optionText: String,
    val optionLabel: String, // A, B, C, D
    val isCorrect: Boolean = false,
    val sortOrder: Int = 0
)

data class ExamQuestionResponse(
    val id: UUID,
    val questionType: QuestionType,
    val questionText: String,
    val imageUrl: String?,
    val points: BigDecimal,
    val sortOrder: Int,
    val correctAnswer: String?,
    val options: List<QuestionOptionResponse>
)

data class QuestionOptionResponse(
    val id: UUID,
    val optionText: String,
    val optionLabel: String,
    val isCorrect: Boolean,
    val sortOrder: Int
)

data class SubmitAnswerRequest(
    val questionId: UUID,
    val selectedOption: String? = null,
    val answerText: String? = null
)

data class ExamAttemptResponse(
    val id: UUID,
    val examId: UUID,
    val examTitle: String,
    val userId: UUID,
    val startedAt: Instant,
    val submittedAt: Instant?,
    val isSubmitted: Boolean,
    val autoScore: BigDecimal?,
    val finalScore: BigDecimal?,
    val durationSeconds: Int?,
    val passed: Boolean?
)

data class ExamAnswerResponse(
    val id: UUID,
    val questionId: UUID,
    val questionText: String,
    val answerText: String?,
    val selectedOption: String?,
    val isCorrect: Boolean?,
    val score: BigDecimal?,
    val correctAnswer: String?
)

data class ExamResultResponse(
    val attempt: ExamAttemptResponse,
    val answers: List<ExamAnswerResponse>
)

// ═══════════════════════════════════════════════════════════════════════════════
// Exam Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class ExamService(
    private val examRepository: ExamRepository,
    private val questionRepository: ExamQuestionRepository,
    private val attemptRepository: ExamAttemptRepository,
    private val answerRepository: ExamAnswerRepository,
    private val userRepository: UserRepository
) {
    // ── Exam CRUD ──

    @Transactional
    fun createExam(creatorId: UUID, request: CreateExamRequest): ExamResponse {
        val creator: User = userRepository.findById(creatorId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        if (creator.role != UserRole.TEACHER && creator.role != UserRole.INSTITUTION && creator.role != UserRole.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only teachers, institutions, or admins can create exams")
        }
        if (request.startsAt.isAfter(request.endsAt)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be before end time")
        }
        val exam = Exam(
            title = request.title,
            description = request.description,
            creator = creator,
            courseId = request.courseId,
            channelId = request.channelId,
            startsAt = request.startsAt,
            endsAt = request.endsAt,
            durationMinutes = request.durationMinutes,
            totalScore = request.totalScore,
            passScore = request.passScore,
            status = ExamStatus.DRAFT,
            isPublic = request.isPublic,
            shuffleQuestions = request.shuffleQuestions,
            shuffleOptions = request.shuffleOptions,
            showResultsAfter = request.showResultsAfter,
            maxAttempts = request.maxAttempts
        )
        val saved: Exam = examRepository.save(exam)
        return mapExamToResponse(saved)
    }

    @Transactional
    fun updateExam(examId: UUID, creatorId: UUID, request: UpdateExamRequest): ExamResponse {
        val exam: Exam = getOwnedExam(examId, creatorId)
        if (exam.status != ExamStatus.DRAFT) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT exams can be edited")
        }
        request.title?.let { exam.title = it }
        request.description?.let { exam.description = it }
        request.startsAt?.let { exam.startsAt = it }
        request.endsAt?.let { exam.endsAt = it }
        request.durationMinutes?.let { exam.durationMinutes = it }
        request.totalScore?.let { exam.totalScore = it }
        request.passScore?.let { exam.passScore = it }
        request.isPublic?.let { exam.isPublic = it }
        request.shuffleQuestions?.let { exam.shuffleQuestions = it }
        request.shuffleOptions?.let { exam.shuffleOptions = it }
        request.showResultsAfter?.let { exam.showResultsAfter = it }
        request.maxAttempts?.let { exam.maxAttempts = it }
        exam.updatedAt = Instant.now()
        val saved: Exam = examRepository.save(exam)
        return mapExamToResponse(saved)
    }

    @Transactional
    fun activateExam(examId: UUID, creatorId: UUID): ExamResponse {
        val exam: Exam = getOwnedExam(examId, creatorId)
        if (exam.status != ExamStatus.DRAFT && exam.status != ExamStatus.SCHEDULED) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT/SCHEDULED exams can be activated")
        }
        val questionCount: Long = questionRepository.countByExamId(examId)
        if (questionCount == 0L) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam must have at least one question")
        }
        exam.status = ExamStatus.ACTIVE
        exam.updatedAt = Instant.now()
        val saved: Exam = examRepository.save(exam)
        return mapExamToResponse(saved)
    }

    @Transactional
    fun endExam(examId: UUID, creatorId: UUID): ExamResponse {
        val exam: Exam = getOwnedExam(examId, creatorId)
        if (exam.status != ExamStatus.ACTIVE) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Only ACTIVE exams can be ended")
        }
        exam.status = ExamStatus.ENDED
        exam.updatedAt = Instant.now()
        val saved: Exam = examRepository.save(exam)
        return mapExamToResponse(saved)
    }

    fun getExamById(examId: UUID): ExamResponse {
        val exam: Exam = examRepository.findById(examId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found") }
        return mapExamToResponse(exam)
    }

    fun getMyExams(creatorId: UUID, pageable: Pageable): Page<ExamResponse> {
        return examRepository.findByCreatorId(creatorId, pageable)
            .map { mapExamToResponse(it) }
    }

    fun getCourseExams(courseId: UUID): List<ExamResponse> {
        return examRepository.findByCourseId(courseId)
            .map { mapExamToResponse(it) }
    }

    fun getChannelExams(channelId: UUID, pageable: Pageable): Page<ExamResponse> {
        return examRepository.findByChannelId(channelId, pageable)
            .map { mapExamToResponse(it) }
    }

    // ── Question Management ──

    @Transactional
    fun addQuestion(examId: UUID, creatorId: UUID, request: AddQuestionRequest): ExamQuestionResponse {
        val exam: Exam = getOwnedExam(examId, creatorId)
        if (exam.status != ExamStatus.DRAFT) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Cannot add questions to a non-DRAFT exam")
        }
        val question = ExamQuestion(
            exam = exam,
            questionType = request.questionType,
            questionText = request.questionText,
            imageUrl = request.imageUrl,
            points = request.points,
            sortOrder = request.sortOrder,
            correctAnswer = request.correctAnswer
        )
        // Add options for multiple choice
        if (request.questionType == QuestionType.MULTIPLE_CHOICE && request.options.isNotEmpty()) {
            val options: MutableList<ExamQuestionOption> = request.options.map { opt ->
                ExamQuestionOption(
                    question = question,
                    optionText = opt.optionText,
                    optionLabel = opt.optionLabel,
                    isCorrect = opt.isCorrect,
                    sortOrder = opt.sortOrder
                )
            }.toMutableList()
            question.options = options
            // Auto-set correctAnswer from option labels
            if (request.correctAnswer == null) {
                val correctLabel: String? = options.firstOrNull { it.isCorrect }?.optionLabel
                question.correctAnswer = correctLabel
            }
        }
        val saved: ExamQuestion = questionRepository.save(question)
        return mapQuestionToResponse(saved, showCorrectAnswer = true)
    }

    @Transactional
    fun deleteQuestion(questionId: UUID, creatorId: UUID) {
        val question: ExamQuestion = questionRepository.findById(questionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found") }
        validateExamOwnership(question.exam!!, creatorId)
        if (question.exam!!.status != ExamStatus.DRAFT) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete questions from a non-DRAFT exam")
        }
        questionRepository.delete(question)
    }

    fun getExamQuestions(examId: UUID, userId: UUID): List<ExamQuestionResponse> {
        val exam: Exam = examRepository.findById(examId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found") }
        val isCreator: Boolean = exam.creator?.id == userId
        val questions: List<ExamQuestion> = questionRepository.findByExamIdOrderBySortOrderAsc(examId)
        return questions.map { mapQuestionToResponse(it, showCorrectAnswer = isCreator) }
    }

    // ── Attempt Lifecycle ──

    @Transactional
    fun startAttempt(examId: UUID, userId: UUID): ExamAttemptResponse {
        val exam: Exam = examRepository.findById(examId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found") }
        if (exam.status != ExamStatus.ACTIVE) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam is not active")
        }
        val now: Instant = Instant.now()
        if (now.isBefore(exam.startsAt) || now.isAfter(exam.endsAt)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam is not within its time window")
        }
        val existingAttempt: ExamAttempt? = attemptRepository.findByExamIdAndUserId(examId, userId)
        if (existingAttempt != null) {
            if (existingAttempt.isSubmitted) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "You have already submitted this exam")
            }
            // Return existing active attempt
            return mapAttemptToResponse(existingAttempt)
        }
        val user: User = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val attempt = ExamAttempt(
            exam = exam,
            user = user,
            startedAt = now,
            isSubmitted = false
        )
        val saved: ExamAttempt = attemptRepository.save(attempt)
        return mapAttemptToResponse(saved)
    }

    @Transactional
    fun submitAnswer(attemptId: UUID, userId: UUID, request: SubmitAnswerRequest): ExamAnswerResponse {
        val attempt: ExamAttempt = getActiveAttempt(attemptId, userId)
        val question: ExamQuestion = questionRepository.findById(request.questionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found") }
        if (question.exam?.id != attempt.exam?.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Question does not belong to this exam")
        }
        // Check time limit
        checkTimeLimit(attempt)
        // Upsert answer
        var answer: ExamAnswer? = answerRepository.findByAttemptIdAndQuestionId(attemptId, request.questionId)
        if (answer == null) {
            answer = ExamAnswer(
                attempt = attempt,
                question = question,
                answerText = request.answerText,
                selectedOption = request.selectedOption
            )
        } else {
            answer.answerText = request.answerText
            answer.selectedOption = request.selectedOption
        }
        // Auto-grade for multiple choice
        if (question.questionType == QuestionType.MULTIPLE_CHOICE && request.selectedOption != null) {
            val isCorrect: Boolean = request.selectedOption == question.correctAnswer
            answer.isCorrect = isCorrect
            answer.score = if (isCorrect) question.points else BigDecimal.ZERO
        }
        val saved: ExamAnswer = answerRepository.save(answer)
        return mapAnswerToResponse(saved)
    }

    @Transactional
    fun submitExam(attemptId: UUID, userId: UUID): ExamAttemptResponse {
        val attempt: ExamAttempt = getActiveAttempt(attemptId, userId)
        attempt.isSubmitted = true
        attempt.submittedAt = Instant.now()
        attempt.durationSeconds = Duration.between(attempt.startedAt, Instant.now()).seconds.toInt()
        // Auto-grade: sum scores for auto-graded answers
        val answers: List<ExamAnswer> = answerRepository.findByAttemptId(attemptId)
        val autoScore: BigDecimal = answers
            .filter { it.score != null }
            .sumOf { it.score!! }
        attempt.autoScore = autoScore
        attempt.finalScore = autoScore // Will be overridden by manual grading if needed
        val saved: ExamAttempt = attemptRepository.save(attempt)
        return mapAttemptToResponse(saved)
    }

    // ── Results ──

    fun getExamResult(attemptId: UUID, userId: UUID): ExamResultResponse {
        val attempt: ExamAttempt = attemptRepository.findById(attemptId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found") }
        val exam: Exam = attempt.exam!!
        val isCreator: Boolean = exam.creator?.id == userId
        val isOwner: Boolean = attempt.user?.id == userId
        if (!isCreator && !isOwner) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        }
        if (!attempt.isSubmitted && !isCreator) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam has not been submitted yet")
        }
        if (!exam.showResultsAfter && !isCreator) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Results are not available for this exam")
        }
        val answers: List<ExamAnswer> = answerRepository.findByAttemptId(attemptId)
        return ExamResultResponse(
            attempt = mapAttemptToResponse(attempt),
            answers = answers.map { mapAnswerToResponse(it) }
        )
    }

    fun getExamAttempts(examId: UUID, creatorId: UUID, pageable: Pageable): Page<ExamAttemptResponse> {
        val exam: Exam = getOwnedExam(examId, creatorId)
        return attemptRepository.findByExamId(exam.id!!, pageable)
            .map { mapAttemptToResponse(it) }
    }

    // ── Manual Grading ──

    @Transactional
    fun gradeAnswer(answerId: UUID, creatorId: UUID, score: BigDecimal): ExamAnswerResponse {
        val answer: ExamAnswer = answerRepository.findById(answerId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found") }
        val exam: Exam = answer.attempt!!.exam!!
        validateExamOwnership(exam, creatorId)
        answer.score = score
        answer.isCorrect = score > BigDecimal.ZERO
        answer.gradedBy = creatorId
        answer.gradedAt = Instant.now()
        val saved: ExamAnswer = answerRepository.save(answer)
        // Recalculate attempt final score
        recalculateAttemptScore(answer.attempt!!.id!!)
        return mapAnswerToResponse(saved)
    }

    @Transactional
    fun markExamAsGraded(examId: UUID, creatorId: UUID): ExamResponse {
        val exam: Exam = getOwnedExam(examId, creatorId)
        if (exam.status != ExamStatus.ENDED) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Only ENDED exams can be marked as graded")
        }
        exam.status = ExamStatus.GRADED
        exam.updatedAt = Instant.now()
        val saved: Exam = examRepository.save(exam)
        return mapExamToResponse(saved)
    }

    // ── Private Helpers ──

    private fun recalculateAttemptScore(attemptId: UUID) {
        val answers: List<ExamAnswer> = answerRepository.findByAttemptId(attemptId)
        val totalScore: BigDecimal = answers
            .filter { it.score != null }
            .sumOf { it.score!! }
        val attempt: ExamAttempt = attemptRepository.findById(attemptId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found") }
        attempt.finalScore = totalScore
        attemptRepository.save(attempt)
    }

    private fun checkTimeLimit(attempt: ExamAttempt) {
        val exam: Exam = attempt.exam!!
        val elapsed: Long = Duration.between(attempt.startedAt, Instant.now()).toMinutes()
        if (elapsed > exam.durationMinutes) {
            attempt.isSubmitted = true
            attempt.submittedAt = Instant.now()
            attempt.durationSeconds = (exam.durationMinutes * 60)
            attemptRepository.save(attempt)
            throw ResponseStatusException(HttpStatus.GONE, "Time limit exceeded. Exam auto-submitted.")
        }
    }

    private fun getActiveAttempt(attemptId: UUID, userId: UUID): ExamAttempt {
        val attempt: ExamAttempt = attemptRepository.findById(attemptId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found") }
        if (attempt.user?.id != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "This is not your attempt")
        }
        if (attempt.isSubmitted) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Exam already submitted")
        }
        return attempt
    }

    private fun getOwnedExam(examId: UUID, creatorId: UUID): Exam {
        val exam: Exam = examRepository.findById(examId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found") }
        validateExamOwnership(exam, creatorId)
        return exam
    }

    private fun validateExamOwnership(exam: Exam, creatorId: UUID) {
        if (exam.creator?.id != creatorId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this exam")
        }
    }

    private fun mapExamToResponse(exam: Exam): ExamResponse {
        val questionCount: Long = questionRepository.countByExamId(exam.id!!)
        val attemptCount: Long = attemptRepository.countByExamIdAndIsSubmittedTrue(exam.id!!)
        return ExamResponse(
            id = exam.id!!,
            title = exam.title,
            description = exam.description,
            creatorId = exam.creator!!.id!!,
            courseId = exam.courseId,
            channelId = exam.channelId,
            startsAt = exam.startsAt,
            endsAt = exam.endsAt,
            durationMinutes = exam.durationMinutes,
            totalScore = exam.totalScore,
            passScore = exam.passScore,
            status = exam.status,
            isPublic = exam.isPublic,
            shuffleQuestions = exam.shuffleQuestions,
            shuffleOptions = exam.shuffleOptions,
            showResultsAfter = exam.showResultsAfter,
            maxAttempts = exam.maxAttempts,
            questionCount = questionCount,
            attemptCount = attemptCount,
            createdAt = exam.createdAt
        )
    }

    private fun mapQuestionToResponse(question: ExamQuestion, showCorrectAnswer: Boolean): ExamQuestionResponse {
        return ExamQuestionResponse(
            id = question.id!!,
            questionType = question.questionType,
            questionText = question.questionText,
            imageUrl = question.imageUrl,
            points = question.points,
            sortOrder = question.sortOrder,
            correctAnswer = if (showCorrectAnswer) question.correctAnswer else null,
            options = question.options.map { opt ->
                QuestionOptionResponse(
                    id = opt.id!!,
                    optionText = opt.optionText,
                    optionLabel = opt.optionLabel,
                    isCorrect = if (showCorrectAnswer) opt.isCorrect else false,
                    sortOrder = opt.sortOrder
                )
            }
        )
    }

    private fun mapAttemptToResponse(attempt: ExamAttempt): ExamAttemptResponse {
        val exam: Exam = attempt.exam!!
        val passed: Boolean? = if (attempt.isSubmitted && exam.passScore != null && attempt.finalScore != null) {
            attempt.finalScore!! >= exam.passScore!!
        } else {
            null
        }
        return ExamAttemptResponse(
            id = attempt.id!!,
            examId = exam.id!!,
            examTitle = exam.title,
            userId = attempt.user!!.id!!,
            startedAt = attempt.startedAt,
            submittedAt = attempt.submittedAt,
            isSubmitted = attempt.isSubmitted,
            autoScore = attempt.autoScore,
            finalScore = attempt.finalScore,
            durationSeconds = attempt.durationSeconds,
            passed = passed
        )
    }

    private fun mapAnswerToResponse(answer: ExamAnswer): ExamAnswerResponse {
        return ExamAnswerResponse(
            id = answer.id!!,
            questionId = answer.question!!.id!!,
            questionText = answer.question!!.questionText,
            answerText = answer.answerText,
            selectedOption = answer.selectedOption,
            isCorrect = answer.isCorrect,
            score = answer.score,
            correctAnswer = answer.question!!.correctAnswer
        )
    }
}
