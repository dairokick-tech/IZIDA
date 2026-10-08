package com.izida.wallet.data.movement

import com.izida.wallet.data.account.AccountRepository
import com.izida.wallet.domain.account.AccountStatus
import com.izida.wallet.domain.account.IzidaAccount
import com.izida.wallet.domain.ledger.Ledger
import com.izida.wallet.domain.ledger.LedgerEntryType
import com.izida.wallet.domain.movement.Movement
import java.time.Instant
import java.math.BigDecimal

class LedgerMovementRepository(
    private val ledger: Ledger
) : AccountRepository {

    override suspend fun getAccount(userId: String): IzidaAccount =
        IzidaAccount(
            id = "ACC-DEMO-000001",
            userId = userId,
            currency = "PEN",
            availableBalance = ledger.balance("ACC-DEMO-000001", "PEN"),
            status = AccountStatus.ACTIVE,
            createdAt = Instant.EPOCH
        )

    override suspend fun getMovements(accountId: String): List<Movement> =
        ledger.entriesFor(accountId).sortedByDescending { it.createdAt }.map { entry ->
            Movement(
                id = entry.id,
                accountId = entry.accountId,
                type = if (entry.type == LedgerEntryType.CREDIT) com.izida.wallet.domain.movement.MovementType.CREDIT
                       else com.izida.wallet.domain.movement.MovementType.DEBIT,
                description = if (entry.type == LedgerEntryType.CREDIT) "Dinero recibido" else "Dinero enviado",
                amount = entry.amount,
                currency = entry.currency,
                createdAt = entry.createdAt,
                reference = entry.transactionId
            )
        }
}
