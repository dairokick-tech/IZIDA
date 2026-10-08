package com.izida.wallet.domain.ledger

import java.math.BigDecimal
import java.time.Instant

enum class TransactionStatus {
    PENDING, PROCESSED, REJECTED, REVERSED
}

enum class LedgerEntryType {
    DEBIT, CREDIT
}

data class LedgerEntry(
    val id: String,
    val transactionId: String,
    val accountId: String,
    val type: LedgerEntryType,
    val amount: BigDecimal,
    val currency: String,
    val createdAt: Instant
)

data class FinancialTransaction(
    val id: String,
    val idempotencyKey: String,
    val sourceAccountId: String,
    val destinationAccountId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: TransactionStatus,
    val createdAt: Instant,
    val reference: String? = null
)
