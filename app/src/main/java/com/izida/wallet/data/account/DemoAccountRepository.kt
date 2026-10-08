package com.izida.wallet.data.account

import com.izida.wallet.domain.account.AccountStatus
import com.izida.wallet.domain.account.IzidaAccount
import com.izida.wallet.domain.movement.Movement
import java.math.BigDecimal
import java.time.Instant

class DemoAccountRepository : AccountRepository {
    private val account = IzidaAccount(
        id = "ACC-DEMO-000001",
        userId = "USR-DEMO-000001",
        currency = "PEN",
        availableBalance = BigDecimal("0.00"),
        status = AccountStatus.ACTIVE,
        createdAt = Instant.now()
    )

    override suspend fun getAccount(userId: String): IzidaAccount = account
    override suspend fun getMovements(accountId: String): List<Movement> = emptyList()
}
