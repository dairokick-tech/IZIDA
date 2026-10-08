package com.izida.wallet.domain.movement

import java.math.BigDecimal
import java.time.Instant

enum class MovementType { CREDIT, DEBIT }

data class Movement(
    val id: String,
    val accountId: String,
    val type: MovementType,
    val description: String,
    val amount: BigDecimal,
    val currency: String,
    val createdAt: Instant,
    val reference: String? = null
)
