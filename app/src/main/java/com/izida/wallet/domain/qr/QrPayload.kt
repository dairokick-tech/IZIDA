package com.izida.wallet.domain.qr

data class IzidaQrPayload(
    val version: Int = 1,
    val accountId: String,
    val displayName: String,
    val currency: String = "PEN"
) {
    fun encode(): String =
        "IZIDA|v=$version|account=$accountId|name=" + displayName.replace("|", "") + "|currency=$currency"

    companion object {
        fun decode(raw: String): IzidaQrPayload? {
            val parts = raw.split("|")
            if (parts.firstOrNull() != "IZIDA") return null
            val values = parts.drop(1).mapNotNull {
                val pair = it.split("=", limit = 2)
                if (pair.size == 2) pair[0] to pair[1] else null
            }.toMap()
            val account = values["account"] ?: return null
            val name = values["name"] ?: return null
            return IzidaQrPayload(
                version = values["v"]?.toIntOrNull() ?: 1,
                accountId = account,
                displayName = name,
                currency = values["currency"] ?: "PEN"
            )
        }
    }
}
