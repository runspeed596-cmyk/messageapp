package com.iliyadev.springboot.services

import com.iliyadev.springboot.repositories.*
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

// ═══════════════════════════════════════════════════════════════════════════════
// Response DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class PlatformStatsResponse(
    val totalUsers: Long,
    val totalChannels: Long,
    val totalGroups: Long,
    val totalCourses: Long,
    val totalExams: Long,
    val totalLockedContents: Long,
    val totalVerifiedTeachers: Long,
    val totalActiveInstitutions: Long,
    val totalActiveSubscriptions: Long,
    val totalWalletTransactions: Long,
    val totalHashtagPromotions: Long,
    val pendingTeacherVerifications: Long,
    val pendingInstitutions: Long,
    val pendingPromotions: Long,
    val generatedAt: Instant
)

data class RevenueStatsResponse(
    val totalDeposits: Long,
    val totalWithdrawals: Long,
    val totalPurchases: Long,
    val totalSubscriptionPayments: Long,
    val totalContentPurchases: Long,
    val totalCourseEnrollments: Long,
    val generatedAt: Instant
)

// ═══════════════════════════════════════════════════════════════════════════════
// Platform Health Service — Phase 7
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class PlatformHealthService(
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository,
    private val groupRepository: GroupRepository,
    private val courseRepository: CourseRepository,
    private val examRepository: ExamRepository,
    private val lockedContentRepository: LockedContentRepository,
    private val teacherVerificationRequestRepository: TeacherVerificationRequestRepository,
    private val institutionRepository: InstitutionRepository,
    private val userSubscriptionRepository: UserSubscriptionRepository,
    private val walletTransactionRepository: WalletTransactionRepository,
    private val hashtagPromotionRepository: HashtagPromotionRepository,
    private val courseEnrollmentRepository: CourseEnrollmentRepository,
    private val contentPurchaseRepository: ContentPurchaseRepository
) {
    fun getPlatformStats(): PlatformStatsResponse {
        return PlatformStatsResponse(
            totalUsers = userRepository.count(),
            totalChannels = channelRepository.count(),
            totalGroups = groupRepository.count(),
            totalCourses = courseRepository.count(),
            totalExams = examRepository.count(),
            totalLockedContents = lockedContentRepository.count(),
            totalVerifiedTeachers = teacherVerificationRequestRepository.countByStatus(com.iliyadev.springboot.models.VerificationStatus.APPROVED),
            totalActiveInstitutions = institutionRepository.countByIsActiveTrue(),
            totalActiveSubscriptions = userSubscriptionRepository.countByIsActiveTrue(),
            totalWalletTransactions = walletTransactionRepository.count(),
            totalHashtagPromotions = hashtagPromotionRepository.count(),
            pendingTeacherVerifications = teacherVerificationRequestRepository.countByStatus(com.iliyadev.springboot.models.VerificationStatus.PENDING_VERIFICATION),
            pendingInstitutions = institutionRepository.countByVerificationStatus(com.iliyadev.springboot.models.VerificationStatus.PENDING_VERIFICATION),
            pendingPromotions = hashtagPromotionRepository.countByModerationStatus(com.iliyadev.springboot.models.ModerationStatus.PENDING),
            generatedAt = Instant.now()
        )
    }

    fun getRevenueStats(): RevenueStatsResponse {
        return RevenueStatsResponse(
            totalDeposits = walletTransactionRepository.countByType(com.iliyadev.springboot.models.TransactionType.DEPOSIT),
            totalWithdrawals = walletTransactionRepository.countByType(com.iliyadev.springboot.models.TransactionType.WITHDRAWAL),
            totalPurchases = walletTransactionRepository.countByType(com.iliyadev.springboot.models.TransactionType.PURCHASE),
            totalSubscriptionPayments = walletTransactionRepository.countByType(com.iliyadev.springboot.models.TransactionType.SUBSCRIPTION),
            totalContentPurchases = contentPurchaseRepository.count(),
            totalCourseEnrollments = courseEnrollmentRepository.count(),
            generatedAt = Instant.now()
        )
    }
}
