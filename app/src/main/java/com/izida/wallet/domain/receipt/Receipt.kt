package com.izida.wallet.domain.receipt

import java.math.BigDecimal
import java.time.Instant

data class PaymentReceipt(
    val transactionId: String,
    val operationType: String,
    val recipientName: String,
    val destinationAccountId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: String,
    val createdAt: Instant,
    val reference: String?
)
