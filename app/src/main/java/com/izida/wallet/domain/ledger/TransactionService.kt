package com.izida.wallet.domain.ledger

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class TransactionService(private val ledger: Ledger) {

    fun transfer(
        sourceAccountId: String,
        destinationAccountId: String,
        amount: BigDecimal,
        currency: String,
        idempotencyKey: String,
        reference: String? = null
    ): Result<FinancialTransaction> {
        if (idempotencyKey.isBlank()) {
            return Result.failure(IllegalArgumentException("La clave de idempotencia es obligatoria"))
        }

        val existing = ledger.findByIdempotencyKey(idempotencyKey)
        if (existing != null) return Result.success(existing)

        val transaction = FinancialTransaction(
            id = "TX-" + UUID.randomUUID().toString(),
            idempotencyKey = idempotencyKey,
            sourceAccountId = sourceAccountId,
            destinationAccountId = destinationAccountId,
            amount = amount,
            currency = currency,
            status = TransactionStatus.PENDING,
            createdAt = Instant.now(),
            reference = reference
        )

        return ledger.post(transaction).map { transaction.copy(status = TransactionStatus.PROCESSED) }
    }
}
