package com.izida.wallet.domain.transfer

interface RecipientDirectory {
    fun findByPhone(phoneNumber: String): Recipient?
}
