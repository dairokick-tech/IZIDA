package com.izida.wallet.data.account

import com.izida.wallet.data.remote.IzidaApi
import com.izida.wallet.domain.account.AccountStatus
import com.izida.wallet.domain.account.IzidaAccount
import com.izida.wallet.domain.movement.Movement
import com.izida.wallet.domain.movement.MovementType
import java.time.Instant

class RemoteAccountRepository(private val api: IzidaApi) : AccountRepository {
    override suspend fun getAccount(userId: String): IzidaAccount {
        val a = api.getMyAccount()
        return IzidaAccount(a.id, a.userId, a.currency, a.balance.toBigDecimal(), AccountStatus.valueOf(a.status), Instant.EPOCH)
    }

    override suspend fun getMovements(accountId: String): List<Movement> =
        api.getMyMovements().map {
            Movement(
                id = it.id,
                accountId = accountId,
                type = if (it.type == "CREDIT") MovementType.CREDIT else MovementType.DEBIT,
                description = if (it.type == "CREDIT") "Dinero recibido" else "Dinero enviado",
                amount = it.amount.toBigDecimal(),
                currency = it.currency,
                createdAt = Instant.parse(it.createdAt),
                reference = it.reference,
                transactionId = it.transactionId,
                status = it.status
            )
        }
}
