package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// Request / Response DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class WalletResponse(
    val id: UUID,
    val userId: UUID,
    val balance: Long,
    val isActive: Boolean,
    val createdAt: Instant
)

data class DepositRequest(
    val amount: Long,
    val gatewayRef: String? = null,
    val description: String? = null
)

data class WithdrawRequest(
    val amount: Long,
    val description: String? = null
)

data class WalletTransactionResponse(
    val id: UUID,
    val type: TransactionType,
    val amount: Long,
    val balanceAfter: Long,
    val description: String?,
    val referenceId: UUID?,
    val referenceType: String?,
    val gatewayRef: String?,
    val createdAt: Instant
)

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val transactionRepository: WalletTransactionRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun getOrCreateWallet(userId: UUID): Wallet {
        val existing: Wallet? = walletRepository.findByUserId(userId)
        if (existing != null) return existing
        val user: User = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val wallet = Wallet(user = user, balance = 0, isActive = true)
        return walletRepository.save(wallet)
    }

    fun getWalletInfo(userId: UUID): WalletResponse {
        val wallet: Wallet = getOrCreateWallet(userId)
        return mapWalletToResponse(wallet)
    }

    @Transactional
    fun deposit(userId: UUID, request: DepositRequest): WalletTransactionResponse {
        if (request.amount <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive")
        }
        val wallet: Wallet = getOrCreateWallet(userId)
        wallet.balance += request.amount
        wallet.updatedAt = Instant.now()
        walletRepository.save(wallet)
        val transaction = WalletTransaction(
            wallet = wallet,
            type = TransactionType.DEPOSIT,
            amount = request.amount,
            balanceAfter = wallet.balance,
            description = request.description ?: "Deposit",
            gatewayRef = request.gatewayRef
        )
        val saved: WalletTransaction = transactionRepository.save(transaction)
        return mapTransactionToResponse(saved)
    }

    @Transactional
    fun withdraw(userId: UUID, request: WithdrawRequest): WalletTransactionResponse {
        if (request.amount <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive")
        }
        val wallet: Wallet = getOrCreateWallet(userId)
        if (wallet.balance < request.amount) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance")
        }
        wallet.balance -= request.amount
        wallet.updatedAt = Instant.now()
        walletRepository.save(wallet)
        val transaction = WalletTransaction(
            wallet = wallet,
            type = TransactionType.WITHDRAWAL,
            amount = request.amount,
            balanceAfter = wallet.balance,
            description = request.description ?: "Withdrawal"
        )
        val saved: WalletTransaction = transactionRepository.save(transaction)
        return mapTransactionToResponse(saved)
    }

    @Transactional
    fun executeInternalPurchase(
        userId: UUID,
        amount: Long,
        description: String,
        referenceId: UUID?,
        referenceType: String?
    ): WalletTransaction {
        if (amount <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive")
        }
        val wallet: Wallet = getOrCreateWallet(userId)
        if (wallet.balance < amount) {
            throw ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Insufficient balance")
        }
        wallet.balance -= amount
        wallet.updatedAt = Instant.now()
        walletRepository.save(wallet)
        val transaction = WalletTransaction(
            wallet = wallet,
            type = TransactionType.PURCHASE,
            amount = amount,
            balanceAfter = wallet.balance,
            description = description,
            referenceId = referenceId,
            referenceType = referenceType
        )
        return transactionRepository.save(transaction)
    }

    @Transactional
    fun refund(userId: UUID, amount: Long, description: String, referenceId: UUID?): WalletTransaction {
        val wallet: Wallet = getOrCreateWallet(userId)
        wallet.balance += amount
        wallet.updatedAt = Instant.now()
        walletRepository.save(wallet)
        val transaction = WalletTransaction(
            wallet = wallet,
            type = TransactionType.REFUND,
            amount = amount,
            balanceAfter = wallet.balance,
            description = description,
            referenceId = referenceId,
            referenceType = "REFUND"
        )
        return transactionRepository.save(transaction)
    }

    fun getTransactionHistory(userId: UUID, pageable: Pageable): Page<WalletTransactionResponse> {
        val wallet: Wallet = getOrCreateWallet(userId)
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.id!!, pageable)
            .map { mapTransactionToResponse(it) }
    }

    private fun mapWalletToResponse(wallet: Wallet): WalletResponse {
        return WalletResponse(
            id = wallet.id!!,
            userId = wallet.user!!.id!!,
            balance = wallet.balance,
            isActive = wallet.isActive,
            createdAt = wallet.createdAt
        )
    }

    private fun mapTransactionToResponse(tx: WalletTransaction): WalletTransactionResponse {
        return WalletTransactionResponse(
            id = tx.id!!,
            type = tx.type,
            amount = tx.amount,
            balanceAfter = tx.balanceAfter,
            description = tx.description,
            referenceId = tx.referenceId,
            referenceType = tx.referenceType,
            gatewayRef = tx.gatewayRef,
            createdAt = tx.createdAt
        )
    }
}
