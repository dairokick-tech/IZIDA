package com.izida.wallet.data.ledger

import com.izida.wallet.domain.ledger.*
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class InMemoryLedger : Ledger {
    private val transactions = ConcurrentHashMap<String, FinancialTransaction>()
    private val entries = ConcurrentHashMap<String, LedgerEntry>()

    override fun balance(accountId: String, currency: String): BigDecimal {
        return entries.values
            .filter { it.accountId == accountId && it.currency == currency }
            .fold(BigDecimal.ZERO) { total, entry ->
                if (entry.type == LedgerEntryType.CREDIT) total.add(entry.amount)
                else total.subtract(entry.amount)
            }
    }

    override fun findByIdempotencyKey(key: String): FinancialTransaction? =
        transactions.values.firstOrNull { it.idempotencyKey == key }

    @Synchronized
    override fun post(transaction: FinancialTransaction): Result<List<LedgerEntry>> {
        if (transaction.amount <= BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("El monto debe ser mayor que cero"))
        }
        if (transaction.sourceAccountId == transaction.destinationAccountId) {
            return Result.failure(IllegalArgumentException("La cuenta origen y destino no pueden ser iguales"))
        }
        if (transaction.currency.isBlank()) {
            return Result.failure(IllegalArgumentException("La moneda es obligatoria"))
        }

        val previous = findByIdempotencyKey(transaction.idempotencyKey)
        if (previous != null) {
            return Result.success(entries.values.filter { it.transactionId == previous.id })
        }

        val sourceBalance = balance(transaction.sourceAccountId, transaction.currency)
        if (sourceBalance < transaction.amount) {
            return Result.failure(IllegalStateException("Saldo insuficiente"))
        }

        val now = Instant.now()
        val debit = LedgerEntry(
            id = transaction.id + "-D",
            transactionId = transaction.id,
            accountId = transaction.sourceAccountId,
            type = LedgerEntryType.DEBIT,
            amount = transaction.amount,
            currency = transaction.currency,
            createdAt = now
        )
        val credit = LedgerEntry(
            id = transaction.id + "-C",
            transactionId = transaction.id,
            accountId = transaction.destinationAccountId,
            type = LedgerEntryType.CREDIT,
            amount = transaction.amount,
            currency = transaction.currency,
            createdAt = now
        )

        entries[debit.id] = debit
        entries[credit.id] = credit
        transactions[transaction.id] = transaction.copy(
            status = TransactionStatus.PROCESSED,
            createdAt = now
        )
        return Result.success(listOf(debit, credit))
    }
}
