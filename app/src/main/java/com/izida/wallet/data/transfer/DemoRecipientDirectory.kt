package com.izida.wallet.data.transfer

import com.izida.wallet.domain.transfer.Recipient
import com.izida.wallet.domain.transfer.RecipientDirectory

class DemoRecipientDirectory : RecipientDirectory {
    private val recipients = listOf(
        Recipient(
            userId = "USR-DEMO-000002",
            accountId = "ACC-DEMO-000002",
            phoneNumber = "999999999",
            displayName = "Usuario IZIDA"
        )
    )

    override fun findByPhone(phoneNumber: String): Recipient? =
        recipients.firstOrNull { it.phoneNumber == phoneNumber }
}
