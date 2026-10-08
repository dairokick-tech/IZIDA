package com.izida.wallet.domain.transfer

data class Recipient(
    val userId: String,
    val accountId: String,
    val phoneNumber: String,
    val displayName: String
)
