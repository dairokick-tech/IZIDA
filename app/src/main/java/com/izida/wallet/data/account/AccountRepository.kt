package com.izida.wallet.data.account

import com.izida.wallet.domain.account.IzidaAccount
import com.izida.wallet.domain.movement.Movement

interface AccountRepository {
    suspend fun getAccount(userId: String): IzidaAccount
    suspend fun getMovements(accountId: String): List<Movement>
}
