package com.izida.wallet.domain.ledger

import java.math.BigDecimal

interface Ledger {
    fun balance(accountId: String, currency: String): BigDecimal
    fun post(transaction: FinancialTransaction): Result<List<LedgerEntry>>
    fun findByIdempotencyKey(key: String): FinancialTransaction?
    fun entriesFor(accountId: String): List<LedgerEntry>
}
