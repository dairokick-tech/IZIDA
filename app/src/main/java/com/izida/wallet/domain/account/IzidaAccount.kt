package com.izida.wallet.domain.account

import java.math.BigDecimal
import java.time.Instant

data class IzidaAccount(
    val id: String,
    val userId: String,
    val currency: String,
    val availableBalance: BigDecimal,
    val status: AccountStatus,
    val createdAt: Instant
)
